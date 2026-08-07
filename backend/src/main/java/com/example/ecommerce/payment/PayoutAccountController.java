package com.example.ecommerce.payment;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.payment.dto.PayoutAccountRequest;
import com.example.ecommerce.payment.dto.PayoutAccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers/payout-account")
public class PayoutAccountController {

    private final PayoutAccountService payoutAccountService;

    public PayoutAccountController(PayoutAccountService payoutAccountService) {
        this.payoutAccountService = payoutAccountService;
    }

    @PostMapping
    public ResponseEntity<PayoutAccountResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal, @Valid @RequestBody PayoutAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payoutAccountService.create(principal.userId(), request));
    }

    @GetMapping
    public PayoutAccountResponse get(@AuthenticationPrincipal JwtPrincipal principal) {
        return payoutAccountService.get(principal.userId());
    }
}
