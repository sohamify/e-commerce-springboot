package com.example.ecommerce.payment;

import com.example.ecommerce.common.exception.PayoutAccountAlreadyExistsException;
import com.example.ecommerce.common.exception.UserNotFoundException;
import com.example.ecommerce.payment.dto.PayoutAccountRequest;
import com.example.ecommerce.payment.dto.PayoutAccountResponse;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayoutAccountServiceImpl implements PayoutAccountService {

    private final SellerPayoutAccountRepository payoutAccountRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;

    public PayoutAccountServiceImpl(
            SellerPayoutAccountRepository payoutAccountRepository,
            UserRepository userRepository,
            RazorpayService razorpayService) {
        this.payoutAccountRepository = payoutAccountRepository;
        this.userRepository = userRepository;
        this.razorpayService = razorpayService;
    }

    @Override
    @Transactional
    public PayoutAccountResponse create(UUID userId, PayoutAccountRequest request) {
        if (payoutAccountRepository.findByUserId(userId).isPresent()) {
            throw new PayoutAccountAlreadyExistsException();
        }
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        RazorpayService.LinkedAccountResult result = razorpayService.createLinkedAccount(user.getEmail(), request);

        SellerPayoutAccount account = SellerPayoutAccount.builder()
            .userId(userId)
            .razorpayAccountId(result.accountId())
            .razorpayProductId(result.productId())
            .status(result.status())
            .build();
        payoutAccountRepository.save(account);

        return new PayoutAccountResponse(account.getStatus());
    }

    @Override
    @Transactional
    public PayoutAccountResponse get(UUID userId) {
        return payoutAccountRepository.findByUserId(userId)
            .map(this::refreshStatus)
            .map(account -> new PayoutAccountResponse(account.getStatus()))
            .orElseGet(() -> new PayoutAccountResponse(null));
    }

    /** Route onboarding activates asynchronously on Razorpay's side, so a status recorded as
     * PENDING at creation time may since have flipped — re-check and persist it whenever the
     * seller looks at their own status, rather than requiring a separate polling job. */
    private SellerPayoutAccount refreshStatus(SellerPayoutAccount account) {
        if (account.getStatus() == PayoutAccountStatus.PENDING) {
            PayoutAccountStatus current = razorpayService.fetchActivationStatus(
                account.getRazorpayAccountId(), account.getRazorpayProductId());
            if (current != account.getStatus()) {
                account.setStatus(current);
                payoutAccountRepository.save(account);
            }
        }
        return account;
    }
}
