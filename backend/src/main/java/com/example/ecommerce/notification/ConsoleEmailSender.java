package com.example.ecommerce.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Dev/test stand-in so the onboarding journey is fully exercisable without real SMTP. */
@Component
@Profile({"dev", "test"})
public class ConsoleEmailSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        log.info("""

            ---- Email (dev/test — not actually sent) ----
            To:      {}
            Subject: {}

            {}
            ------------------------------------------------""",
            message.to(), message.subject(), message.htmlBody());
    }
}
