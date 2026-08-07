package com.example.ecommerce.payment;

import com.example.ecommerce.payment.dto.PayoutAccountRequest;
import com.example.ecommerce.payment.dto.PayoutAccountResponse;
import java.util.UUID;

public interface PayoutAccountService {

    PayoutAccountResponse create(UUID userId, PayoutAccountRequest request);

    PayoutAccountResponse get(UUID userId);
}
