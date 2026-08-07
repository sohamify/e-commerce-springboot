package com.example.ecommerce.payment;

import com.example.ecommerce.common.exception.InvalidPaymentSignatureException;
import com.example.ecommerce.common.exception.RazorpayIntegrationException;
import com.example.ecommerce.config.RazorpayProperties;
import com.example.ecommerce.payment.dto.PayoutAccountRequest;
import com.razorpay.Account;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The only class in the codebase that talks to the Razorpay SDK/API directly — every other
 * class works with our own DTOs and entities. Keeping SDK-specific field names and call
 * sequencing in one place makes it the single spot to update if Razorpay's API changes.
 */
@Service
public class RazorpayService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayService.class);

    /** Rupees <-> paise: every Razorpay amount field is an integer number of paise. */
    private static final int PAISE_PER_RUPEE = 100;

    private final RazorpayClient client;
    private final RazorpayProperties properties;

    public RazorpayService(RazorpayClient client, RazorpayProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public record LinkedAccountResult(String accountId, String productId, PayoutAccountStatus status) {
    }

    /**
     * Create Linked Account -> create Stakeholder -> request the "route" product configuration
     * -> submit bank settlement details against it, per Razorpay's documented onboarding
     * sequence (https://razorpay.com/docs/payments/route/onboarding/). All four calls must
     * succeed for the seller to end up with a usable payout account, so any failure partway
     * through surfaces as a single RazorpayIntegrationException rather than leaving a half-built
     * account silently recorded.
     */
    public LinkedAccountResult createLinkedAccount(String email, PayoutAccountRequest request) {
        try {
            JSONObject accountRequest = new JSONObject();
            accountRequest.put("email", email);
            accountRequest.put("phone", request.phone());
            accountRequest.put("legal_business_name", request.legalBusinessName());
            accountRequest.put("business_type", request.businessType());
            accountRequest.put("contact_name", request.contactName());
            Account account = client.account.create(accountRequest);
            String accountId = account.get("id");

            JSONObject stakeholderRequest = new JSONObject();
            stakeholderRequest.put("name", request.contactName());
            stakeholderRequest.put("email", email);
            JSONObject phone = new JSONObject();
            phone.put("primary", request.phone());
            stakeholderRequest.put("phone", phone);
            JSONObject kyc = new JSONObject();
            kyc.put("pan", request.pan());
            stakeholderRequest.put("kyc", kyc);
            client.stakeholder.create(accountId, stakeholderRequest);

            JSONObject productRequest = new JSONObject();
            productRequest.put("product_name", "route");
            productRequest.put("tnc_accepted", true);
            Account productConfig = client.product.requestProductConfiguration(accountId, productRequest);
            String productId = productConfig.get("id");

            JSONObject settlements = new JSONObject();
            settlements.put("account_number", request.bankAccountNumber());
            settlements.put("ifsc_code", request.ifscCode());
            settlements.put("beneficiary_name", request.beneficiaryName());
            JSONObject updateRequest = new JSONObject();
            updateRequest.put("settlements", settlements);
            updateRequest.put("tnc_accepted", true);
            Account updated = client.product.edit(accountId, productId, updateRequest);

            return new LinkedAccountResult(accountId, productId, mapActivationStatus(updated.get("activation_status")));
        } catch (RazorpayException e) {
            throw wrap("Could not set up the payout account with Razorpay", e);
        }
    }

    /** Re-checks the current activation status of an already-created Linked Account's route
     * product configuration — Route onboarding is asynchronous on Razorpay's side, so a status
     * recorded as PENDING at creation time can flip to ACTIVE later without us doing anything. */
    public PayoutAccountStatus fetchActivationStatus(String accountId, String productId) {
        try {
            Account productConfig = client.product.fetch(accountId, productId);
            return mapActivationStatus(productConfig.get("activation_status"));
        } catch (RazorpayException e) {
            throw wrap("Could not check payout account status with Razorpay", e);
        }
    }

    private static PayoutAccountStatus mapActivationStatus(String activationStatus) {
        if (activationStatus == null) {
            return PayoutAccountStatus.PENDING;
        }
        return switch (activationStatus) {
            case "activated" -> PayoutAccountStatus.ACTIVE;
            case "suspended", "rejected" -> PayoutAccountStatus.REJECTED;
            default -> PayoutAccountStatus.PENDING; // requested, needs_clarification, under_review
        };
    }

    public record OrderResult(String orderId) {
    }

    /**
     * Plain order, no Route transfer — the whole amount lands in the platform's own Razorpay
     * balance. Used while {@code app.razorpay.route-enabled} is false (Route isn't approved on
     * the account yet); everything downstream (Checkout, signature verification, the webhook)
     * behaves identically either way, since a payment.captured event doesn't care whether the
     * order it came from had transfers.
     */
    public OrderResult createOrder(BigDecimal amount) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", toPaise(amount));
            orderRequest.put("currency", "INR");

            Order order = client.orders.create(orderRequest);
            return new OrderResult(order.get("id"));
        } catch (RazorpayException e) {
            throw wrap("Could not create the Razorpay order", e);
        }
    }

    /** Creates a Razorpay Order carrying a single Route transfer to the seller's linked account
     * for {@code amount - platformFee}, so the split happens automatically at capture time
     * instead of requiring a manual settlement afterward. Only called once Route is enabled. */
    public OrderResult createOrderWithTransfer(BigDecimal amount, BigDecimal platformFee, String sellerAccountId) {
        try {
            long amountPaise = toPaise(amount);
            long sellerAmountPaise = toPaise(amount.subtract(platformFee));

            JSONObject transfer = new JSONObject();
            transfer.put("account", sellerAccountId);
            transfer.put("amount", sellerAmountPaise);
            transfer.put("currency", "INR");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("transfers", new JSONArray().put(transfer));

            Order order = client.orders.create(orderRequest);
            return new OrderResult(order.get("id"));
        } catch (RazorpayException e) {
            throw wrap("Could not create the Razorpay order", e);
        }
    }

    /** Recomputes the Checkout signature server-side (HMAC-SHA256 of "order_id|payment_id" keyed
     * on the API key secret) and throws rather than returning false, so callers can't
     * accidentally ignore a failed check. */
    public void verifyCheckoutSignature(String orderId, String paymentId, String signature) {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);
        try {
            if (!Utils.verifyPaymentSignature(options, properties.keySecret())) {
                throw new InvalidPaymentSignatureException();
            }
        } catch (RazorpayException e) {
            throw new InvalidPaymentSignatureException();
        }
    }

    /** Verifies X-Razorpay-Signature against the *raw* webhook body — must run before the body
     * is parsed as JSON for anything else. */
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        try {
            return Utils.verifyWebhookSignature(rawBody, signature, properties.webhookSecret());
        } catch (RazorpayException e) {
            return false;
        }
    }

    /** Refunds the buyer and reverses every Route transfer made on this payment in one call
     * (Razorpay's {@code reverse_all} flag), per
     * https://razorpay.com/docs/api/payments/route/refund-payments-and-reverse-transfer/ —
     * reversing the seller's share and refunding the buyer both happen server-side atomically
     * rather than needing two separately-sequenced calls on our end. */
    public void refundWithTransferReversal(String razorpayPaymentId, BigDecimal amount) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", toPaise(amount));
            refundRequest.put("reverse_all", true);
            client.payments.refund(razorpayPaymentId, refundRequest);
        } catch (RazorpayException e) {
            throw wrap("Could not refund the payment with Razorpay", e);
        }
    }

    private static long toPaise(BigDecimal rupees) {
        return rupees.multiply(BigDecimal.valueOf(PAISE_PER_RUPEE)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** Logs Razorpay's actual error message (the SDK's {@code RazorpayException} carries only a
     * message/cause — no structured status/code/field accessors) before collapsing it into the
     * generic 502 the client gets back, so the real reason a request was rejected is at least
     * visible server-side. */
    private static RazorpayIntegrationException wrap(String message, RazorpayException e) {
        log.error("Razorpay API call failed: {} - {}", message, e.getMessage(), e);
        return new RazorpayIntegrationException(message + ": " + e.getMessage(), e);
    }
}
