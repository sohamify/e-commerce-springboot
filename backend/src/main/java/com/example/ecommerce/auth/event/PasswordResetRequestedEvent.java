package com.example.ecommerce.auth.event;

import com.example.ecommerce.user.UserRole;
import java.util.UUID;

public record PasswordResetRequestedEvent(UUID userId, String email, UserRole role) {
}
