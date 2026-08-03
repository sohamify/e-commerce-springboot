package com.example.ecommerce.messaging;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByThreadIdOrderByCreatedAtAsc(UUID threadId);

    Optional<Message> findFirstByThreadIdOrderByCreatedAtDesc(UUID threadId);
}
