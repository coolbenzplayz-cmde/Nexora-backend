package org.example.nexora.media.entity;

import org.example.nexora.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_versions", indexes = {
        @Index(name = "idx_versions_job", columnList = "editing_job_id"),
        @Index(name = "idx_versions_user", columnList = "user_id"),
        @Index(name = "idx_versions_number", columnList = "version_number")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ContentVersion extends BaseEntity {

    @Column(name = "editing_job_id", nullable = false)
    private Long editingJobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "content_uri", nullable = false, length = 1024)
    private String contentUri;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "format")
    private String format;

    @Column(name = "is_current")
    private Boolean isCurrent = false;

    @Column(name = "thumbnail_uri", length = 1024)
    private String thumbnailUri;
}
