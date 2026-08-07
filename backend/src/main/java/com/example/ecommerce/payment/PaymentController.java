package com.example.ecommerce.payment;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.config.RazorpayProperties;
import com.example.ecommerce.payment.dto.PaymentsConfigResponse;
import com.example.ecommerce.payment.dto.VerifyPaymentRequest;
import com.example.ecommerce.payment.dto.VerifyPaymentResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayProperties razorpayProperties;

    public PaymentController(PaymentService paymentService, RazorpayProperties razorpayProperties) {
        this.paymentService = paymentService;
        this.razorpayProperties = razorpayProperties;
    }

    /** Lets the frontend skip the "set up payouts" gate while Route isn't enabled yet. */
    @GetMapping("/config")
    public PaymentsConfigResponse config() {
        return new PaymentsConfigResponse(razorpayProperties.routeEnabled());
    }

    /** Client-driven confirmation, for immediate UI feedback right after Checkout closes.
     * Recomputes the signature server-side before trusting anything the client sent — the
     * payment.captured webhook (RazorpayWebhookController) is still the authoritative path and
     * wins if the two ever disagree. */
    @PostMapping("/verify")
    public VerifyPaymentResponse verify(
            @AuthenticationPrincipal JwtPrincipal principal, @Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(principal.userId(), request);
    }

    /** Admin-only per SecurityConfig's URL-pattern gate — no dispute/moderation workflow exists
     * yet to justify letting sellers or buyers trigger this themselves. */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Void> refund(@PathVariable UUID paymentId) {
        paymentService.refund(paymentId);
        return ResponseEntity.noContent().build();
    }
}
