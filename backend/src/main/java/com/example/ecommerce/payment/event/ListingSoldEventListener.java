package com.example.ecommerce.payment.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Placeholder reaction to a completed sale — same in-process event pattern as
 * {@code auth.event.RegistrationEmailListener}, not real GCP Pub/Sub (nothing in this codebase
 * publishes to Pub/Sub today). Future reactions (buyer/seller emails, analytics) attach here
 * without PaymentService needing to know about them.
 */
@Component
public class ListingSoldEventListener {

    private static final Logger log = LoggerFactory.getLogger(ListingSoldEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onListingSold(ListingSoldEvent event) {
        log.info("Listing {} sold to buyer {} (seller {}, payment {})",
            event.listingId(), event.buyerId(), event.sellerId(), event.paymentId());
    }
}
