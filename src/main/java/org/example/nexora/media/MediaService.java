package org.example.nexora.media;

import org.example.nexora.common.BusinessException;
import org.example.nexora.concurrency.ConcurrencyGuard;
import org.example.nexora.media.dto.*;
import org.example.nexora.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class MediaService {

    private final EditingJobRepository editingJobRepository;
    private final ContentTemplateRepository templateRepository;
    private final ContentVersionRepository versionRepository;
    private final ContentAnalyticsRepository analyticsRepository;
    private final ContentCollaborationRepository collaborationRepository;
    private final ConcurrencyGuard concurrencyGuard;

    public MediaService(EditingJobRepository editingJobRepository,
                       ContentTemplateRepository templateRepository,
                       ContentVersionRepository versionRepository,
                       ContentAnalyticsRepository analyticsRepository,
                       ContentCollaborationRepository collaborationRepository,
                       ConcurrencyGuard concurrencyGuard) {
        this.editingJobRepository = editingJobRepository;
        this.templateRepository = templateRepository;
        this.versionRepository = versionRepository;
        this.analyticsRepository = analyticsRepository;
        this.collaborationRepository = collaborationRepository;
        this.concurrencyGuard = concurrencyGuard;
    }

    @Transactional
    public EditingJob createJob(User user, CreateEditingJobRequest request) {
        ConcurrencyGuard.TaskConfig config = ConcurrencyGuard.TaskConfig.builder()
            .rateLimit(50)
            .rateWindowSeconds(60)
            .timeoutSeconds(10)
            .priority(ConcurrencyGuard.AsyncTaskProcessor.TaskPriority.NORMAL)
            .build();

        return concurrencyGuard.executeSafely(
            "createEditingJob",
            "media-service",
            () -> {
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
            },
            () -> {
                throw new BusinessException("Service temporarily unavailable", "SERVICE_UNAVAILABLE");
            },
            config
        ).join();
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

    // Video Editing Capabilities
    @Transactional
    public CompletableFuture<EditingJob> createVideoEditingJobAsync(User user, VideoEditingRequest request) {
        ConcurrencyGuard.TaskConfig config = ConcurrencyGuard.TaskConfig.builder()
            .rateLimit(20)
            .rateWindowSeconds(60)
            .timeoutSeconds(30)
            .priority(ConcurrencyGuard.AsyncTaskProcessor.TaskPriority.HIGH)
            .build();

        return concurrencyGuard.executeSafely(
            "createVideoEditingJob",
            "media-video-service",
            () -> {
                EditingJob job = new EditingJob();
                job.setUserId(user.getId());
                job.setJobType(EditingJob.EditingJobType.VIDEO);
                job.setSourceUri(request.getSourceUri());
                job.setStatus(EditingJob.EditingJobStatus.QUEUED);
                job.setEditingConfig(request.getEditingConfig());
                return editingJobRepository.save(job);
            },
            () -> {
                EditingJob fallbackJob = new EditingJob();
                fallbackJob.setUserId(user.getId());
                fallbackJob.setJobType(EditingJob.EditingJobType.VIDEO);
                fallbackJob.setStatus(EditingJob.EditingJobStatus.FAILED);
                fallbackJob.setErrorMessage("Service temporarily unavailable");
                return fallbackJob;
            },
            config
        );
    }

    @Transactional
    public EditingJob createVideoEditingJob(User user, VideoEditingRequest request) {
        return createVideoEditingJobAsync(user, request).join();
    }

    @Transactional
    public EditingJob applyVideoEffects(User user, Long jobId, List<VideoEffect> effects) {
        EditingJob job = getJob(user, jobId);
        job.setEditingConfig(Map.of("effects", effects));
        job.setStatus(EditingJob.EditingJobStatus.PROCESSING);
        return editingJobRepository.save(job);
    }

    // Photo/Image Editing Capabilities
    @Transactional
    public EditingJob createPhotoEditingJob(User user, PhotoEditingRequest request) {
        EditingJob job = new EditingJob();
        job.setUserId(user.getId());
        job.setJobType(EditingJob.EditingJobType.IMAGE);
        job.setSourceUri(request.getSourceUri());
        job.setStatus(EditingJob.EditingJobStatus.QUEUED);
        job.setEditingConfig(Map.of("filters", request.getFilters(), "adjustments", request.getAdjustments()));
        return editingJobRepository.save(job);
    }

    @Transactional
    public EditingJob applyImageFilters(User user, Long jobId, List<ImageFilter> filters) {
        EditingJob job = getJob(user, jobId);
        job.setEditingConfig(Map.of("filters", filters));
        job.setStatus(EditingJob.EditingJobStatus.PROCESSING);
        return editingJobRepository.save(job);
    }

    // Content Template System
    @Transactional
    public ContentTemplate createTemplate(User user, CreateTemplateRequest request) {
        ContentTemplate template = new ContentTemplate();
        template.setUserId(user.getId());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTemplateType(request.getTemplateType());
        template.setThumbnailUri(request.getThumbnailUri());
        template.setTemplateData(request.getTemplateData());
        template.setIsPublic(request.getIsPublic());
        return templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<ContentTemplate> getTemplates(User user, String templateType) {
        if (templateType != null) {
            return templateRepository.findByTemplateTypeAndIsPublicOrUserId(templateType, user.getId());
        }
        return templateRepository.findByIsPublicOrUserId(user.getId());
    }

    @Transactional
    public EditingJob createFromTemplate(User user, CreateFromTemplateRequest request) {
        ContentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new BusinessException("Template not found", "TEMPLATE_NOT_FOUND"));
        
        EditingJob job = new EditingJob();
        job.setUserId(user.getId());
        job.setJobType(EditingJob.EditingJobType.valueOf(template.getTemplateType().toUpperCase()));
        job.setSourceUri(request.getSourceUri());
        job.setStatus(EditingJob.EditingJobStatus.QUEUED);
        job.setEditingConfig(Map.of("templateId", template.getId(), "customizations", request.getCustomizations()));
        return editingJobRepository.save(job);
    }

    // Content Collaboration
    @Transactional
    public ContentCollaboration inviteCollaborator(User user, Long jobId, InviteCollaboratorRequest request) {
        EditingJob job = getJob(user, jobId);
        
        ContentCollaboration collaboration = new ContentCollaboration();
        collaboration.setEditingJobId(jobId);
        collaboration.setOwnerId(user.getId());
        collaboration.setCollaboratorId(request.getCollaboratorId());
        collaboration.setPermission(request.getPermission());
        collaboration.setStatus(ContentCollaboration.CollaborationStatus.PENDING);
        collaboration.setExpiresAt(request.getExpiresAt());
        return collaborationRepository.save(collaboration);
    }

    @Transactional
    public ContentCollaboration acceptCollaboration(User user, Long collaborationId) {
        ContentCollaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new BusinessException("Collaboration not found", "COLLABORATION_NOT_FOUND"));
        
        if (!collaboration.getCollaboratorId().equals(user.getId())) {
            throw new BusinessException("Forbidden", "FORBIDDEN");
        }
        
        collaboration.setStatus(ContentCollaboration.CollaborationStatus.ACTIVE);
        collaboration.setJoinedAt(LocalDateTime.now());
        return collaborationRepository.save(collaboration);
    }

    // Content Versioning
    @Transactional
    public ContentVersion createVersion(User user, Long jobId, CreateVersionRequest request) {
        EditingJob job = getJob(user, jobId);
        
        ContentVersion version = new ContentVersion();
        version.setEditingJobId(jobId);
        version.setUserId(user.getId());
        version.setVersionNumber(request.getVersionNumber());
        version.setContentUri(request.getContentUri());
        version.setChanges(request.getChanges());
        version.setMetadata(request.getMetadata());
        return versionRepository.save(version);
    }

    @Transactional(readOnly = true)
    public List<ContentVersion> getVersionHistory(User user, Long jobId) {
        EditingJob job = getJob(user, jobId);
        return versionRepository.findByEditingJobIdOrderByVersionNumberDesc(jobId);
    }

    @Transactional
    public EditingJob restoreVersion(User user, Long jobId, Long versionId) {
        EditingJob job = getJob(user, jobId);
        ContentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException("Version not found", "VERSION_NOT_FOUND"));
        
        job.setSourceUri(version.getContentUri());
        job.setEditingConfig(version.getMetadata());
        return editingJobRepository.save(job);
    }

    // Content Scheduling
    @Transactional
    public EditingJob scheduleContent(User user, Long jobId, ScheduleRequest request) {
        EditingJob job = getJob(user, jobId);
        job.setScheduledAt(request.getScheduledAt());
        job.setPublishingPlatforms(request.getPublishingPlatforms());
        job.setStatus(EditingJob.EditingJobStatus.SCHEDULED);
        return editingJobRepository.save(job);
    }

    // Content Analytics
    @Transactional
    public ContentAnalytics trackAnalytics(User user, Long jobId, AnalyticsEvent event) {
        EditingJob job = getJob(user, jobId);
        
        ContentAnalytics analytics = new ContentAnalytics();
        analytics.setEditingJobId(jobId);
        analytics.setUserId(user.getId());
        analytics.setEventType(event.getEventType());
        analytics.setPlatform(event.getPlatform());
        analytics.setMetrics(event.getMetrics());
        analytics.setTimestamp(LocalDateTime.now());
        return analyticsRepository.save(analytics);
    }

    @Transactional(readOnly = true)
    public ContentAnalytics getAnalytics(User user, Long jobId, String timeRange) {
        EditingJob job = getJob(user, jobId);
        // Implementation for analytics aggregation based on time range
        return analyticsRepository.findTopByEditingJobIdOrderByTimestampDesc(jobId);
    }

    // Content Monetization
    @Transactional
    public EditingJob enableMonetization(User user, Long jobId, MonetizationSettings settings) {
        EditingJob job = getJob(user, jobId);
        job.setMonetizationEnabled(true);
        job.setMonetizationSettings(settings);
        return editingJobRepository.save(job);
    }

    // Content Sharing
    @Transactional
    public ShareLink createShareLink(User user, Long jobId, ShareLinkRequest request) {
        EditingJob job = getJob(user, jobId);
        
        ShareLink shareLink = new ShareLink();
        shareLink.setEditingJobId(jobId);
        shareLink.setUserId(user.getId());
        shareLink.setToken(java.util.UUID.randomUUID().toString());
        shareLink.setExpiresAt(request.getExpiresAt());
        shareLink.setPermission(request.getPermission());
        shareLink.setPassword(request.getPassword());
        return shareLink; // Assuming repository save
    }

    // Advanced Editing Features
    @Transactional
    public CompletableFuture<EditingJob> applyAdvancedEditingAsync(User user, Long jobId, AdvancedEditingRequest request) {
        ConcurrencyGuard.TaskConfig config = ConcurrencyGuard.TaskConfig.builder()
            .rateLimit(10)
            .rateWindowSeconds(60)
            .timeoutSeconds(60)
            .priority(ConcurrencyGuard.AsyncTaskProcessor.TaskPriority.HIGH)
            .build();

        return concurrencyGuard.executeWithResourceManagement(
            "applyAdvancedEditing",
            "video-processing",
            () -> {
                // Simulate video processing resource
                return "video-processor-" + jobId;
            },
            (resource) -> {
                EditingJob job = getJob(user, jobId);
                job.setEditingConfig(Map.of(
                    "advancedEffects", request.getAdvancedEffects(),
                    "transitions", request.getTransitions(),
                    "audioEnhancements", request.getAudioEnhancements(),
                    "colorGrading", request.getColorGrading()
                ));
                job.setStatus(EditingJob.EditingJobStatus.PROCESSING);
                return editingJobRepository.save(job);
            },
            () -> {
                EditingJob fallbackJob = getJob(user, jobId);
                fallbackJob.setStatus(EditingJob.EditingJobStatus.FAILED);
                fallbackJob.setErrorMessage("Advanced editing failed due to resource constraints");
                return fallbackJob;
            }
        );
    }

    @Transactional
    public EditingJob applyAdvancedEditing(User user, Long jobId, AdvancedEditingRequest request) {
        return applyAdvancedEditingAsync(user, jobId, request).join();
    }

    @Transactional
    public CompletableFuture<Void> batchProcessAsync(User user, BatchProcessingRequest request) {
        ConcurrencyGuard.BatchConfig config = ConcurrencyGuard.BatchConfig.builder()
            .maxConcurrentTasks(5)
            .timeoutMinutes(30)
            .continueOnError(true)
            .priority(ConcurrencyGuard.AsyncTaskProcessor.TaskPriority.LOW)
            .build();

        AsyncTaskProcessor.BatchTask batchTask = new AsyncTaskProcessor.BatchTask() {
            @Override
            public String getTaskId() {
                return "batch-process-" + user.getId() + "-" + System.currentTimeMillis();
            }

            @Override
            public void execute() throws Exception {
                // Create batch job
                EditingJob batchJob = new EditingJob();
                batchJob.setUserId(user.getId());
                batchJob.setJobType(EditingJob.EditingJobType.VIDEO);
                batchJob.setStatus(EditingJob.EditingJobStatus.QUEUED);
                batchJob.setEditingConfig(Map.of(
                    "batchFiles", request.getFileUris(),
                    "batchOperations", request.getOperations()
                ));
                editingJobRepository.save(batchJob);
                
                // Process each file in the batch
                for (String fileUri : request.getFileUris()) {
                    // Simulate processing
                    Thread.sleep(100); // Simulate processing time
                }
            }
        };

        return concurrencyGuard.executeBatchTask("media-batch-processing", batchTask, config);
    }

    @Transactional
    public EditingJob batchProcess(User user, BatchProcessingRequest request) {
        batchProcessAsync(user, request).join();
        // Return a summary job
        EditingJob summaryJob = new EditingJob();
        summaryJob.setUserId(user.getId());
        summaryJob.setJobType(EditingJob.EditingJobType.VIDEO);
        summaryJob.setStatus(EditingJob.EditingJobStatus.COMPLETED);
        summaryJob.setResultUri("batch-completed");
        return summaryJob;
    }

    @Transactional
    public void deleteJob(User user, Long jobId) {
        EditingJob job = getJob(user, jobId);
        editingJobRepository.delete(job);
    }

    @Transactional
    public EditingJob updateJobStatus(Long jobId, EditingJob.EditingJobStatus status, String resultUri, String errorMessage) {
        EditingJob job = editingJobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", "JOB_NOT_FOUND"));
        job.setStatus(status);
        if (resultUri != null) job.setResultUri(resultUri);
        if (errorMessage != null) job.setErrorMessage(errorMessage);
        return editingJobRepository.save(job);
    }
}
