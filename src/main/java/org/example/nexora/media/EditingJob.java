package org.example.nexora.media;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "media_editing_jobs", indexes = {
        @Index(name = "idx_media_jobs_user", columnList = "user_id"),
        @Index(name = "idx_media_jobs_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EditingJob extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private EditingJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EditingJobStatus status = EditingJobStatus.QUEUED;

    @Column(name = "source_uri", length = 1024)
    private String sourceUri;

    @Column(name = "result_uri", length = 1024)
    private String resultUri;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public enum EditingJobType {
        IMAGE,
        VIDEO,
        AUDIO
    }

    public enum EditingJobStatus {
        QUEUED,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    // Explicit getters and setters to ensure they exist
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public EditingJobType getJobType() {
        return jobType;
    }

    public void setJobType(EditingJobType jobType) {
        this.jobType = jobType;
    }

    public EditingJobStatus getStatus() {
        return status;
    }

    public void setStatus(EditingJobStatus status) {
        this.status = status;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }

    public String getResultUri() {
        return resultUri;
    }

    public void setResultUri(String resultUri) {
        this.resultUri = resultUri;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
