package org.example.nexora.media;

import lombok.Data;
import org.example.nexora.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Content version entity
 */
@Entity
@Table(name = "content_versions")
@Data
public class ContentVersion extends BaseEntity {
    
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    
    @Column(nullable = false)
    private int version;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "version_notes", columnDefinition = "TEXT")
    private String versionNotes;
    
    @Column(name = "is_current")
    private boolean isCurrent = false;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        this.createdAt = LocalDateTime.now();
    }
}
