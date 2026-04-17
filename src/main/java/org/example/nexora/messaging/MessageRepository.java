package org.example.nexora.messaging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    List<Message> findByConversationIdAndSenderIdOrConversationIdAndReceiverIdOrderByCreatedAtAsc(
        Long conversationId, Long senderId, Long conversationId2, Long receiverId);
    List<Message> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);
    long countByConversationIdAndReceiverIdAndIsReadFalse(Long conversationId, Long receiverId);
    List<Message> findTop20ByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
