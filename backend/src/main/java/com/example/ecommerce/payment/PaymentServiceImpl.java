package com.example.ecommerce.payment;

import com.example.ecommerce.common.exception.CannotBuyOwnListingException;
import com.example.ecommerce.common.exception.InvalidPaymentSignatureException;
import com.example.ecommerce.common.exception.ListingNotFoundException;
import com.example.ecommerce.common.exception.ListingUnavailableException;
import com.example.ecommerce.common.exception.PaymentNotFoundException;
import com.example.ecommerce.common.exception.PaymentNotRefundableException;
import com.example.ecommerce.common.exception.SellerPayoutNotReadyException;
import com.example.ecommerce.config.RazorpayProperties;
import com.example.ecommerce.listing.Listing;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.ListingService;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.payment.dto.PurchaseInitiationResponse;
import com.example.ecommerce.payment.dto.VerifyPaymentRequest;
import com.example.ecommerce.payment.dto.VerifyPaymentResponse;
import com.example.ecommerce.payment.event.ListingSoldEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final ListingRepository listingRepository;
    private final ListingService listingService;
    private final SellerPayoutAccountRepository payoutAccountRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final RazorpayProperties razorpayProperties;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentServiceImpl(
            ListingRepository listingRepository,
            ListingService listingService,
            SellerPayoutAccountRepository payoutAccountRepository,
            PaymentRepository paymentRepository,
            RazorpayService razorpayService,
            RazorpayProperties razorpayProperties,
            ApplicationEventPublisher eventPublisher) {
        this.listingRepository = listingRepository;
        this.listingService = listingService;
        this.payoutAccountRepository = payoutAccountRepository;
        this.paymentRepository = paymentRepository;
        this.razorpayService = razorpayService;
        this.razorpayProperties = razorpayProperties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public PurchaseInitiationResponse initiatePurchase(UUID buyerId, UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ListingUnavailableException();
        }
        if (listing.getSellerId().equals(buyerId)) {
            throw new CannotBuyOwnListingException();
        }

        SellerPayoutAccount payoutAccount = payoutAccountRepository.findByUserId(listing.getSellerId())
            .filter(account -> account.getStatus() == PayoutAccountStatus.ACTIVE)
            .orElseThrow(SellerPayoutNotReadyException::new);

        BigDecimal platformFee = listing.getPrice()
            .multiply(razorpayProperties.platformFeePercent())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        RazorpayService.OrderResult order = razorpayService.createOrderWithTransfer(
            listing.getPrice(), platformFee, payoutAccount.getRazorpayAccountId());

        Payment payment = Payment.builder()
            .listingId(listingId)
            .buyerId(buyerId)
            .sellerId(listing.getSellerId())
            .razorpayOrderId(order.orderId())
            .amount(listing.getPrice())
            .platformFeeAmount(platformFee)
            .status(PaymentStatus.CREATED)
            .build();
        paymentRepository.save(payment);

        return new PurchaseInitiationResponse(order.orderId(), razorpayProperties.keyId(), listing.getPrice(), "INR");
    }

    @Override
    @Transactional
    public VerifyPaymentResponse verifyPayment(UUID buyerId, VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
            .orElseThrow(PaymentNotFoundException::new);
        // Don't reveal that an order belongs to someone else — same 404 as "doesn't exist".
        if (!payment.getBuyerId().equals(buyerId)) {
            throw new PaymentNotFoundException();
        }

        try {
            razorpayService.verifyCheckoutSignature(
                request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature());
        } catch (InvalidPaymentSignatureException e) {
            paymentRepository.markFailed(request.razorpayOrderId());
            throw e;
        }

        boolean sold = captureAndClaim(request.razorpayOrderId(), request.razorpayPaymentId());
        return new VerifyPaymentResponse(sold, payment.getListingId());
    }

    @Override
    @Transactional
    public void handleCaptured(String razorpayOrderId, String razorpayPaymentId) {
        captureAndClaim(razorpayOrderId, razorpayPaymentId);
    }

    @Override
    @Transactional
    public void handleFailed(String razorpayOrderId) {
        paymentRepository.markFailed(razorpayOrderId);
    }

    /**
     * Shared by /verify and the payment.captured webhook — whichever call arrives first wins the
     * guarded {@code markCaptured} update; the other is a safe no-op. Then attempts to claim the
     * listing for this payment's buyer; if that loses a race against a different buyer's payment
     * for the same listing, this payment is refunded (transfer-reversed) instead of leaving the
     * buyer charged for nothing.
     */
    private boolean captureAndClaim(String razorpayOrderId, String razorpayPaymentId) {
        int updated = paymentRepository.markCaptured(razorpayOrderId, razorpayPaymentId);
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
            .orElseThrow(PaymentNotFoundException::new);

        if (updated == 0) {
            // Already processed by the other of /verify or the webhook for this same order.
            return payment.getStatus() == PaymentStatus.CAPTURED;
        }

        try {
            listingService.claimForBuyer(payment.getBuyerId(), payment.getListingId());
            eventPublisher.publishEvent(new ListingSoldEvent(
                payment.getListingId(), payment.getBuyerId(), payment.getSellerId(), payment.getId()));
            return true;
        } catch (ListingUnavailableException e) {
            log.warn("Payment {} captured but listing {} was already sold — refunding", payment.getId(), payment.getListingId());
            razorpayService.refundWithTransferReversal(razorpayPaymentId, payment.getAmount());
            paymentRepository.markRefunded(payment.getId());
            return false;
        }
    }

    @Override
    @Transactional
    public void refund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new PaymentNotRefundableException();
        }

        razorpayService.refundWithTransferReversal(payment.getRazorpayPaymentId(), payment.getAmount());

        int updated = paymentRepository.markRefunded(paymentId);
        if (updated == 0) {
            throw new PaymentNotRefundableException();
        }
        listingService.revertToActive(payment.getListingId(), payment.getBuyerId());
    }
}
