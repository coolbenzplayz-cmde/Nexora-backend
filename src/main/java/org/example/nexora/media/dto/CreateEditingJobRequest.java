package org.example.nexora.media.dto;

import lombok.Data;
import org.example.nexora.media.EditingJob;

@Data
public class CreateEditingJobRequest {
    private EditingJob.EditingJobType jobType;
    private String sourceUri;

    // Explicit getters to ensure they exist
    public EditingJob.EditingJobType getJobType() {
        return jobType;
    }

    public void setJobType(EditingJob.EditingJobType jobType) {
        this.jobType = jobType;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }
}
