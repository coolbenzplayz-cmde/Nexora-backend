package org.example.nexora.video;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "VideoComment")
@Table(name = "video_comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;

    private Long userId;

    private String content;

    private Long parentId; // for replies

    private LocalDateTime createdAt = LocalDateTime.now();

    public Comment() {}

    public Comment(Long videoId, Long userId, String content, Long parentId) {
        this.videoId = videoId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
    }

    public Long getId() { return id; }
    public Long getVideoId() { return videoId; }
    public Long getUserId() { return userId; }
    public String getContent() { return content; }
    public Long getParentId() { return parentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}