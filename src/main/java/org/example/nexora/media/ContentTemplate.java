package org.example.nexora.media;

import lombok.Data;
import org.example.nexora.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Content template entity
 */
@Entity
@Table(name = "content_templates")
@Data
public class ContentTemplate extends BaseEntity {
    
    @Column(nullable = false)
    private String templateName;
    
    @Column(nullable = false)
    private String templateType; // VIDEO, IMAGE, AUDIO, DOCUMENT
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String templateData; // JSON data for template configuration
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "preview_url")
    private String previewUrl;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "is_public")
    private boolean isPublic = false;
    
    @Column(name = "usage_count")
    private int usageCount = 0;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        this.createdAt = LocalDateTime.now();
    }
}
