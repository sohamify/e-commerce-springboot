package com.example.ecommerce.auth.event;

import com.example.ecommerce.auth.passwordreset.PasswordResetService;
import com.example.ecommerce.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetEmailListener {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailListener.class);

    private final PasswordResetService passwordResetService;
    private final NotificationSender notificationSender;
    private final FrontendLinkResolver linkResolver;

    public PasswordResetEmailListener(PasswordResetService passwordResetService,
                                       NotificationSender notificationSender,
                                       FrontendLinkResolver linkResolver) {
        this.passwordResetService = passwordResetService;
        this.notificationSender = notificationSender;
        this.linkResolver = linkResolver;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            String rawToken = passwordResetService.issueToken(event.userId());
            String link = linkResolver.passwordResetLink(event.role(), rawToken);
            notificationSender.send(AuthEmailContentFactory.passwordResetEmail(event.email(), link));
        } catch (RuntimeException e) {
            log.error("Failed to send password-reset email to {}", event.email(), e);
        }
    }
}
