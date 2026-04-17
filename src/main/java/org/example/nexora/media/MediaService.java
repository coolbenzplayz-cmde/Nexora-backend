package org.example.nexora.media;

import org.example.nexora.common.BusinessException;
import org.example.nexora.media.dto.CreateEditingJobRequest;
import org.example.nexora.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MediaService {

    private final EditingJobRepository editingJobRepository;

    public MediaService(EditingJobRepository editingJobRepository) {
        this.editingJobRepository = editingJobRepository;
    }

    @Transactional
    public EditingJob createJob(User user, CreateEditingJobRequest request) {
        if (request.getJobType() == null) {
            throw new BusinessException("jobType is required", "JOB_TYPE_REQUIRED");
        }
        if (request.getSourceUri() == null || request.getSourceUri().isBlank()) {
            throw new BusinessException("sourceUri is required", "SOURCE_REQUIRED");
        }
        EditingJob job = new EditingJob();
        job.setUserId(user.getId());
        job.setJobType(request.getJobType());
        job.setSourceUri(request.getSourceUri().trim());
        job.setStatus(EditingJob.EditingJobStatus.QUEUED);
        return editingJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public EditingJob getJob(User user, Long jobId) {
        EditingJob job = editingJobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", "JOB_NOT_FOUND"));
        if (!job.getUserId().equals(user.getId())) {
            throw new BusinessException("Forbidden", "FORBIDDEN");
        }
        return job;
    }

    @Transactional(readOnly = true)
    public List<EditingJob> listMyJobs(User user) {
        return editingJobRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}
