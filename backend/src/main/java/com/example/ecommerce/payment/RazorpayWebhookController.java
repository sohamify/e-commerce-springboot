package com.example.ecommerce.payment;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authoritative source of truth for payment state (see PaymentService.captureAndClaim) —
 * everything /api/payments/verify does for immediate UI feedback, this repeats independently
 * of whether the buyer's browser is even still open. Public per SecurityConfig (Razorpay has no
 * JWT to present); authenticity comes entirely from the signature check below, not from Spring
 * Security's filter chain.
 */
@RestController
@RequestMapping("/api/webhooks/razorpay")
public class RazorpayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(RazorpayWebhookController.class);

    private final RazorpayService razorpayService;
    private final PaymentService paymentService;

    public RazorpayWebhookController(RazorpayService razorpayService, PaymentService paymentService) {
        this.razorpayService = razorpayService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Void> handle(
            HttpServletRequest request, @RequestHeader("X-Razorpay-Signature") String signature) throws IOException {
        // Signature is computed over the exact raw bytes Razorpay sent — must be verified before
        // this body is parsed as JSON for anything else, and before any of it is trusted.
        String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!razorpayService.verifyWebhookSignature(rawBody, signature)) {
            log.warn("Rejected Razorpay webhook with an invalid signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        JSONObject payload = new JSONObject(rawBody);
        String event = payload.optString("event", "");
        JSONObject payloadSection = payload.optJSONObject("payload");
        JSONObject paymentSection = payloadSection == null ? null : payloadSection.optJSONObject("payment");
        JSONObject paymentEntity = paymentSection == null ? null : paymentSection.optJSONObject("entity");
        String orderId = paymentEntity == null ? null : paymentEntity.optString("order_id", null);
        String paymentId = paymentEntity == null ? null : paymentEntity.optString("id", null);

        if (orderId == null) {
            // Not a payment event we care about (or a shape we don't recognize) — acknowledge
            // so Razorpay doesn't keep retrying, but do nothing.
            return ResponseEntity.ok().build();
        }

        switch (event) {
            case "payment.captured" -> paymentService.handleCaptured(orderId, paymentId);
            case "payment.failed" -> paymentService.handleFailed(orderId);
            default -> log.debug("Ignoring unhandled Razorpay webhook event: {}", event);
        }

        return ResponseEntity.ok().build();
    }
}
