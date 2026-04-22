package org.example.nexora.media.dto;

import lombok.Data;
import org.example.nexora.media.entity.ContentCollaboration;

import java.time.LocalDateTime;

@Data
public class InviteCollaboratorRequest {
    private Long collaboratorId;
    private ContentCollaboration.CollaborationPermission permission;
    private String message;
    private LocalDateTime expiresAt;
}
