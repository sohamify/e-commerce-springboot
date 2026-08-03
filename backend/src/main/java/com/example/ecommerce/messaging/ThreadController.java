package com.example.ecommerce.messaging;

import com.example.ecommerce.auth.jwt.JwtPrincipal;
import com.example.ecommerce.messaging.dto.MessageRequest;
import com.example.ecommerce.messaging.dto.MessageResponse;
import com.example.ecommerce.messaging.dto.ThreadSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final MessagingService messagingService;

    public ThreadController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping
    public List<ThreadSummaryResponse> mine(@AuthenticationPrincipal JwtPrincipal principal) {
        return messagingService.myThreads(principal.userId());
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@AuthenticationPrincipal JwtPrincipal principal, @PathVariable UUID id) {
        return messagingService.listMessages(principal.userId(), id);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Void> send(
            @AuthenticationPrincipal JwtPrincipal principal, @PathVariable UUID id, @Valid @RequestBody MessageRequest request) {
        messagingService.sendMessage(principal.userId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
