package com.example.ecommerce.messaging;

import com.example.ecommerce.messaging.dto.MessageRequest;
import com.example.ecommerce.messaging.dto.MessageResponse;
import com.example.ecommerce.messaging.dto.ThreadSummaryResponse;
import java.util.List;
import java.util.UUID;

public interface MessagingService {

    /** Starts a new thread on first contact, or reuses the buyer's existing one for this listing. */
    ThreadSummaryResponse startThread(UUID buyerId, UUID listingId, MessageRequest request);

    void sendMessage(UUID senderId, UUID threadId, MessageRequest request);

    List<ThreadSummaryResponse> myThreads(UUID userId);

    List<MessageResponse> listMessages(UUID userId, UUID threadId);
}
