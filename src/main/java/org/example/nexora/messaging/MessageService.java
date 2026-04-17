package org.example.nexora.messaging;

import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageRepository messageRepository, 
                         UserRepository userRepository,
                         SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Message sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("Receiver not found"));

        Long conversationId = generateConversationId(senderId, receiverId);

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setConversationId(conversationId);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                savedMessage
        );

        return savedMessage;
    }

    public Page<Message> getConversationMessages(Long userId1, Long userId2, Pageable pageable) {
        Long conversationId = generateConversationId(userId1, userId2);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    public List<Message> getRecentMessages(Long userId, int limit) {
        return messageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(userId);
    }

    public Message markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("Message not found"));

        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("Unauthorized to mark this message as read");
        }

        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public void markConversationAsRead(Long userId, Long conversationId) {
        List<Message> unreadMessages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, Pageable.unpaged())
                .getContent();
        
        for (Message message : unreadMessages) {
            if (!message.getIsRead() && message.getReceiverId().equals(userId)) {
                message.setIsRead(true);
                message.setReadAt(LocalDateTime.now());
                messageRepository.save(message);
            }
        }
    }

    public long getUnreadCount(Long userId, Long conversationId) {
        return messageRepository.countByConversationIdAndReceiverIdAndIsReadFalse(conversationId, userId);
    }

    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("Message not found"));
    }

    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("Unauthorized to delete this message");
        }

        messageRepository.delete(message);
    }

    private Long generateConversationId(Long userId1, Long userId2) {
        return userId1 < userId2 ? 
                userId1 * 1000000 + userId2 : 
                userId2 * 1000000 + userId1;
    }

    public List<Message> getUserConversations(Long userId) {
        return messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId);
    }
}
