package com.example.ecommerce.auth.event;

import com.example.ecommerce.auth.verification.EmailVerificationService;
import com.example.ecommerce.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to registration (and resends) by issuing a verification token and emailing it.
 * AuthServiceImpl never depends on EmailVerificationService's write side or NotificationSender —
 * it only publishes the event, so adding another reaction (e.g. analytics) never touches it.
 *
 * Runs after the registering transaction commits (fallback to immediate execution if there
 * isn't one, e.g. a direct unit-test call) and never lets an email-delivery failure surface to
 * the caller — registration must succeed even if SMTP is down.
 */
@Component
public class RegistrationEmailListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEmailListener.class);

    private final EmailVerificationService verificationService;
    private final NotificationSender notificationSender;
    private final FrontendLinkResolver linkResolver;

    public RegistrationEmailListener(EmailVerificationService verificationService,
                                      NotificationSender notificationSender,
                                      FrontendLinkResolver linkResolver) {
        this.verificationService = verificationService;
        this.notificationSender = notificationSender;
        this.linkResolver = linkResolver;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            String rawToken = verificationService.issueToken(event.userId());
            String link = linkResolver.verificationLink(event.role(), rawToken);
            notificationSender.send(AuthEmailContentFactory.verificationEmail(event.email(), link));
        } catch (RuntimeException e) {
            log.error("Failed to send verification email to {}", event.email(), e);
        }
    }
}
