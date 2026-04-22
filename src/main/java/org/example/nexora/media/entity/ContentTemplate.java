package org.example.nexora.media.entity;

import org.example.nexora.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_templates", indexes = {
        @Index(name = "idx_templates_user", columnList = "user_id"),
        @Index(name = "idx_templates_type", columnList = "template_type"),
        @Index(name = "idx_templates_public", columnList = "is_public")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ContentTemplate extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 50)
    private TemplateType templateType;

    @Column(name = "thumbnail_uri", length = 1024)
    private String thumbnailUri;

    @Column(name = "template_data", columnDefinition = "JSON")
    private String templateData;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "usage_count")
    private Long usageCount = 0L;

    @Column(name = "rating")
    private Double rating = 0.0;

    public enum TemplateType {
        VIDEO,
        IMAGE,
        AUDIO,
        CAROUSEL,
        STORY,
        REEL,
        THUMBNAIL,
        BANNER
    }
}
