package com.example.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.ecommerce.common.exception.InvalidPaymentSignatureException;
import com.example.ecommerce.config.RazorpayProperties;
import com.razorpay.RazorpayClient;
import java.math.BigDecimal;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RazorpayServiceTest {

    private static final String KEY_SECRET = "test-only-razorpay-key-secret";
    private static final String WEBHOOK_SECRET = "test-only-razorpay-webhook-secret";

    private RazorpayService razorpayService;

    @BeforeEach
    void setUp() throws Exception {
        // RazorpayClient's constructor only stores credentials — no network call — so it's safe
        // to build directly in a unit test.
        RazorpayClient client = new RazorpayClient("rzp_test_0000000000", KEY_SECRET);
        RazorpayProperties properties = new RazorpayProperties("rzp_test_0000000000", KEY_SECRET, WEBHOOK_SECRET, BigDecimal.TEN, true);
        razorpayService = new RazorpayService(client, properties);
    }

    @Test
    void verifyCheckoutSignature_matchingSignature_doesNotThrow() {
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = hmacSha256(orderId + "|" + paymentId, KEY_SECRET);

        assertDoesNotThrowSignature(orderId, paymentId, signature);
    }

    @Test
    void verifyCheckoutSignature_tamperedSignature_throws() {
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = hmacSha256(orderId + "|" + "someone-elses-payment", KEY_SECRET);

        assertThrows(InvalidPaymentSignatureException.class,
            () -> razorpayService.verifyCheckoutSignature(orderId, paymentId, signature));
    }

    @Test
    void verifyCheckoutSignature_wrongSecret_throws() {
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = hmacSha256(orderId + "|" + paymentId, "not-the-real-secret");

        assertThrows(InvalidPaymentSignatureException.class,
            () -> razorpayService.verifyCheckoutSignature(orderId, paymentId, signature));
    }

    @Test
    void verifyWebhookSignature_matchingSignature_returnsTrue() {
        String body = "{\"event\":\"payment.captured\"}";
        String signature = hmacSha256(body, WEBHOOK_SECRET);

        assertTrue(razorpayService.verifyWebhookSignature(body, signature));
    }

    @Test
    void verifyWebhookSignature_tamperedBody_returnsFalse() {
        String signedBody = "{\"event\":\"payment.captured\"}";
        String signature = hmacSha256(signedBody, WEBHOOK_SECRET);
        String tamperedBody = "{\"event\":\"payment.failed\"}";

        assertFalse(razorpayService.verifyWebhookSignature(tamperedBody, signature));
    }

    private void assertDoesNotThrowSignature(String orderId, String paymentId, String signature) {
        razorpayService.verifyCheckoutSignature(orderId, paymentId, signature);
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
