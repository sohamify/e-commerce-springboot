package com.example.ecommerce.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID id, UUID senderId, String body, Instant createdAt) {
}
