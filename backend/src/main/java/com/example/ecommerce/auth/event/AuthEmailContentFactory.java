package com.example.ecommerce.auth.event;

import com.example.ecommerce.notification.EmailMessage;

/** Builds the subject/body for each auth-related email. Adding a new email type is one new method here. */
public final class AuthEmailContentFactory {

    private AuthEmailContentFactory() {
    }

    public static EmailMessage verificationEmail(String to, String verificationLink) {
        String body = """
            <p>Welcome! Please verify your email address to activate your account.</p>
            <p><a href="%s">Verify my email</a></p>
            <p>This link expires in 24 hours. If you didn't create this account, you can ignore this email.</p>
            """.formatted(verificationLink);
        return new EmailMessage(to, "Verify your email", body);
    }

    public static EmailMessage passwordResetEmail(String to, String resetLink) {
        String body = """
            <p>We received a request to reset your password.</p>
            <p><a href="%s">Reset my password</a></p>
            <p>This link expires in 1 hour. If you didn't request this, you can safely ignore this email.</p>
            """.formatted(resetLink);
        return new EmailMessage(to, "Reset your password", body);
    }
}
