package com.example.ecommerce.notification;

/**
 * Strategy for delivering an {@link EmailMessage}. Swapping providers (console logging,
 * SMTP, a future transactional-email API) means adding a new implementation, not touching
 * any caller.
 */
public interface NotificationSender {

    void send(EmailMessage message);
}
