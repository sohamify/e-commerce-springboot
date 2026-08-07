package com.example.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ecommerce.common.exception.CannotBuyOwnListingException;
import com.example.ecommerce.common.exception.ListingUnavailableException;
import com.example.ecommerce.common.exception.SellerPayoutNotReadyException;
import com.example.ecommerce.config.RazorpayProperties;
import com.example.ecommerce.listing.Listing;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.ListingService;
import com.example.ecommerce.listing.ListingStatus;
import com.example.ecommerce.payment.dto.PurchaseInitiationResponse;
import com.example.ecommerce.payment.event.ListingSoldEvent;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ListingService listingService;
    @Mock
    private SellerPayoutAccountRepository payoutAccountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RazorpayService razorpayService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentServiceImpl paymentService;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final UUID listingId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RazorpayProperties properties = new RazorpayProperties("rzp_test_key", "secret", "webhook-secret", BigDecimal.TEN);
        paymentService = new PaymentServiceImpl(
            listingRepository, listingService, payoutAccountRepository, paymentRepository,
            razorpayService, properties, eventPublisher);
    }

    private Listing activeListing() {
        return Listing.builder()
            .id(listingId)
            .sellerId(sellerId)
            .price(new BigDecimal("100.00"))
            .status(ListingStatus.ACTIVE)
            .build();
    }

    private SellerPayoutAccount activePayoutAccount() {
        return SellerPayoutAccount.builder()
            .userId(sellerId)
            .razorpayAccountId("acc_seller123")
            .razorpayProductId("acc_prd_123")
            .status(PayoutAccountStatus.ACTIVE)
            .build();
    }

    @Test
    void initiatePurchase_activeListingAndActivePayoutAccount_computesTenPercentFee() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(activeListing()));
        when(payoutAccountRepository.findByUserId(sellerId)).thenReturn(Optional.of(activePayoutAccount()));
        when(razorpayService.createOrderWithTransfer(eq(new BigDecimal("100.00")), eq(new BigDecimal("10.00")), eq("acc_seller123")))
            .thenReturn(new RazorpayService.OrderResult("order_abc"));

        PurchaseInitiationResponse response = paymentService.initiatePurchase(buyerId, listingId);

        assertEquals("order_abc", response.orderId());
        assertEquals(new BigDecimal("100.00"), response.amount());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(new BigDecimal("10.00"), captor.getValue().getPlatformFeeAmount());
        assertEquals(PaymentStatus.CREATED, captor.getValue().getStatus());
    }

    @Test
    void initiatePurchase_sellerHasNoActivePayoutAccount_throws() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(activeListing()));
        when(payoutAccountRepository.findByUserId(sellerId)).thenReturn(Optional.empty());

        assertThrows(SellerPayoutNotReadyException.class, () -> paymentService.initiatePurchase(buyerId, listingId));
    }

    @Test
    void initiatePurchase_listingNotActive_throws() {
        Listing sold = activeListing();
        sold.setStatus(ListingStatus.SOLD);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(sold));

        assertThrows(ListingUnavailableException.class, () -> paymentService.initiatePurchase(buyerId, listingId));
    }

    @Test
    void initiatePurchase_buyerIsSeller_throws() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(activeListing()));

        assertThrows(CannotBuyOwnListingException.class, () -> paymentService.initiatePurchase(sellerId, listingId));
    }

    private Payment createdPayment() {
        return Payment.builder()
            .id(UUID.randomUUID())
            .listingId(listingId)
            .buyerId(buyerId)
            .sellerId(sellerId)
            .razorpayOrderId("order_abc")
            .amount(new BigDecimal("100.00"))
            .platformFeeAmount(new BigDecimal("10.00"))
            .status(PaymentStatus.CREATED)
            .build();
    }

    @Test
    void handleCaptured_listingStillActive_claimsAndPublishesEvent() {
        Payment payment = createdPayment();
        when(paymentRepository.markCaptured("order_abc", "pay_xyz")).thenReturn(1);
        when(paymentRepository.findByRazorpayOrderId("order_abc")).thenReturn(Optional.of(payment));

        paymentService.handleCaptured("order_abc", "pay_xyz");

        verify(listingService).claimForBuyer(buyerId, listingId);
        verify(eventPublisher).publishEvent(any(ListingSoldEvent.class));
        verify(razorpayService, never()).refundWithTransferReversal(anyString(), any());
    }

    @Test
    void handleCaptured_listingAlreadySoldToSomeoneElse_refundsInsteadOfLeavingBuyerCharged() {
        Payment payment = createdPayment();
        when(paymentRepository.markCaptured("order_abc", "pay_xyz")).thenReturn(1);
        when(paymentRepository.findByRazorpayOrderId("order_abc")).thenReturn(Optional.of(payment));
        org.mockito.Mockito.doThrow(new ListingUnavailableException())
            .when(listingService).claimForBuyer(buyerId, listingId);

        paymentService.handleCaptured("order_abc", "pay_xyz");

        verify(razorpayService).refundWithTransferReversal("pay_xyz", new BigDecimal("100.00"));
        verify(paymentRepository).markRefunded(payment.getId());
        verify(eventPublisher, never()).publishEvent(any(ListingSoldEvent.class));
    }

    @Test
    void handleCaptured_alreadyProcessedByVerifyEndpoint_doesNotClaimTwice() {
        Payment payment = createdPayment();
        payment.setStatus(PaymentStatus.CAPTURED);
        // Guarded UPDATE ... WHERE status = CREATED matches nothing a second time.
        when(paymentRepository.markCaptured("order_abc", "pay_xyz")).thenReturn(0);
        when(paymentRepository.findByRazorpayOrderId("order_abc")).thenReturn(Optional.of(payment));

        paymentService.handleCaptured("order_abc", "pay_xyz");

        verify(listingService, never()).claimForBuyer(any(), any());
        verify(razorpayService, never()).refundWithTransferReversal(anyString(), any());
        verify(paymentRepository, times(1)).findByRazorpayOrderId("order_abc");
    }
}
