package com.example.ecommerce.auth.event;

import com.example.ecommerce.user.UserRole;
import java.util.UUID;

/** Published on both first registration and an explicit resend-verification request. */
public record UserRegisteredEvent(UUID userId, String email, UserRole role) {
}
