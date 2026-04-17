package org.example.nexora.messaging;

import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Message>> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        Message message = messageService.sendMessage(senderId, receiverId, content);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<ApiResponse<PaginationResponse<Message>>> getConversationMessages(
            @PathVariable Long userId1,
            @PathVariable Long userId2,
            Pageable pageable) {
        Page<Message> messages = messageService.getConversationMessages(userId1, userId2, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PaginationResponse<>(messages)));
    }

    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<ApiResponse<List<Message>>> getRecentMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getRecentMessages(userId, limit)));
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<Message>> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.markAsRead(messageId, userId)));
    }

    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(
            @PathVariable Long conversationId,
            @RequestParam Long userId) {
        messageService.markConversationAsRead(userId, conversationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @PathVariable Long conversationId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("count", messageService.getUnreadCount(userId, conversationId))));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Message>> getMessage(@PathVariable Long messageId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessageById(messageId)));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/{userId}/conversations")
    public ResponseEntity<ApiResponse<List<Message>>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getUserConversations(userId)));
    }

    @MessageMapping("/chat")
    public void handleChatMessage(@Payload Message message) {
        if (message.getReceiverId() != null) {
            messageService.sendMessage(message.getSenderId(), message.getReceiverId(), message.getContent());
        }
    }
}
