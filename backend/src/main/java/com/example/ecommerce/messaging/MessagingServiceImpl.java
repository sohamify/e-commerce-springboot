package com.example.ecommerce.messaging;

import com.example.ecommerce.common.exception.CannotMessageOwnListingException;
import com.example.ecommerce.common.exception.ListingNotFoundException;
import com.example.ecommerce.common.exception.ThreadAccessDeniedException;
import com.example.ecommerce.common.exception.ThreadNotFoundException;
import com.example.ecommerce.listing.Listing;
import com.example.ecommerce.listing.ListingPhoto;
import com.example.ecommerce.listing.ListingPhotoRepository;
import com.example.ecommerce.listing.ListingRepository;
import com.example.ecommerce.listing.dto.SellerSummaryResponse;
import com.example.ecommerce.messaging.dto.MessageRequest;
import com.example.ecommerce.messaging.dto.MessageResponse;
import com.example.ecommerce.messaging.dto.ThreadSummaryResponse;
import com.example.ecommerce.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessagingServiceImpl implements MessagingService {

    private final MessageThreadRepository messageThreadRepository;
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;
    private final ListingPhotoRepository listingPhotoRepository;
    private final UserRepository userRepository;

    public MessagingServiceImpl(
            MessageThreadRepository messageThreadRepository,
            MessageRepository messageRepository,
            ListingRepository listingRepository,
            ListingPhotoRepository listingPhotoRepository,
            UserRepository userRepository) {
        this.messageThreadRepository = messageThreadRepository;
        this.messageRepository = messageRepository;
        this.listingRepository = listingRepository;
        this.listingPhotoRepository = listingPhotoRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ThreadSummaryResponse startThread(UUID buyerId, UUID listingId, MessageRequest request) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);
        if (listing.getSellerId().equals(buyerId)) {
            throw new CannotMessageOwnListingException();
        }

        MessageThread thread = messageThreadRepository.findByListingIdAndBuyerId(listingId, buyerId)
            .orElseGet(() -> messageThreadRepository.save(MessageThread.builder()
                .listingId(listingId)
                .buyerId(buyerId)
                .sellerId(listing.getSellerId())
                .build()));

        appendMessage(thread, buyerId, request.body());
        return toThreadSummary(thread, buyerId);
    }

    @Override
    @Transactional
    public void sendMessage(UUID senderId, UUID threadId, MessageRequest request) {
        MessageThread thread = requireThread(threadId);
        requireParticipant(thread, senderId);
        appendMessage(thread, senderId, request.body());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThreadSummaryResponse> myThreads(UUID userId) {
        return messageThreadRepository.findByBuyerIdOrSellerIdOrderByUpdatedAtDesc(userId, userId).stream()
            .map(thread -> toThreadSummary(thread, userId))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(UUID userId, UUID threadId) {
        MessageThread thread = requireThread(threadId);
        requireParticipant(thread, userId);
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId).stream()
            .map(m -> new MessageResponse(m.getId(), m.getSenderId(), m.getBody(), m.getCreatedAt()))
            .toList();
    }

    private void appendMessage(MessageThread thread, UUID senderId, String body) {
        messageRepository.save(Message.builder().threadId(thread.getId()).senderId(senderId).body(body).build());
        // @UpdateTimestamp only fires on an actual UPDATE — touching the field here guarantees
        // one, so the thread sorts to the top of the inbox on every new message.
        thread.setUpdatedAt(Instant.now());
        messageThreadRepository.save(thread);
    }

    private ThreadSummaryResponse toThreadSummary(MessageThread thread, UUID viewerId) {
        Listing listing = listingRepository.findById(thread.getListingId()).orElse(null);
        String listingTitle = listing != null ? listing.getTitle() : "(listing removed)";
        String photoUrl = listingPhotoRepository.findByListingIdOrderByPositionAsc(thread.getListingId()).stream()
            .findFirst()
            .map(ListingPhoto::getUrl)
            .orElse(null);

        UUID counterpartyId = viewerId.equals(thread.getBuyerId()) ? thread.getSellerId() : thread.getBuyerId();
        SellerSummaryResponse counterparty = userRepository.findById(counterpartyId)
            .map(SellerSummaryResponse::from)
            .orElse(null);

        Message last = messageRepository.findFirstByThreadIdOrderByCreatedAtDesc(thread.getId()).orElse(null);

        return new ThreadSummaryResponse(
            thread.getId(),
            thread.getListingId(),
            listingTitle,
            photoUrl,
            counterparty,
            last == null ? null : last.getBody(),
            last == null ? thread.getCreatedAt() : last.getCreatedAt());
    }

    private MessageThread requireThread(UUID threadId) {
        return messageThreadRepository.findById(threadId).orElseThrow(ThreadNotFoundException::new);
    }

    private void requireParticipant(MessageThread thread, UUID userId) {
        if (!thread.getBuyerId().equals(userId) && !thread.getSellerId().equals(userId)) {
            throw new ThreadAccessDeniedException();
        }
    }
}
