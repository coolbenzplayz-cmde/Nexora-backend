package org.example.nexora.media.dto;

import lombok.Data;
import org.example.nexora.media.entity.ContentCollaboration;

import java.time.LocalDateTime;

@Data
public class ShareLinkRequest {
    private ContentCollaboration.CollaborationPermission permission;
    private LocalDateTime expiresAt;
    private String password;
    private Boolean allowDownload = false;
    private Boolean allowComment = false;
    private Integer maxViews;
    private String customMessage;
}
