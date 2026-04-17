package org.example.nexora.messaging;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.util.UUID;

/**
 * Message entity for real-time messaging
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_conversation_id", columnList = "conversationId"),
        @Index(name = "idx_messages_sender_id", columnList = "senderId")
})
public class Message extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "media_urls", columnDefinition = "TEXT[]")
    private String[] mediaUrls;

    @Column(name = "reply_to_id")
    private UUID replyToId;

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        FILE,
        LINK
    }
}