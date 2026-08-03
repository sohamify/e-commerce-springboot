package com.example.ecommerce.notification;

/** Transport-agnostic representation of a single email to deliver. */
public record EmailMessage(String to, String subject, String htmlBody) {
}
