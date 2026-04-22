package org.example.nexora.media;

import org.example.nexora.common.ApiResponse;
import org.example.nexora.media.dto.*;
import org.example.nexora.media.entity.*;
import org.example.nexora.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // Basic Job Management
    @PostMapping("/editing/jobs")
    public ResponseEntity<ApiResponse<EditingJob>> createJob(
            Authentication authentication,
            @RequestBody CreateEditingJobRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.createJob(user, request)));
    }

    @GetMapping("/editing/jobs/{jobId}")
    public ResponseEntity<ApiResponse<EditingJob>> getJob(
            Authentication authentication,
            @PathVariable Long jobId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.getJob(user, jobId)));
    }

    @GetMapping("/editing/jobs")
    public ResponseEntity<ApiResponse<List<EditingJob>>> listJobs(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.listMyJobs(user)));
    }

    @DeleteMapping("/editing/jobs/{jobId}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            Authentication authentication,
            @PathVariable Long jobId) {
        User user = (User) authentication.getPrincipal();
        mediaService.deleteJob(user, jobId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Video Editing
    @PostMapping("/editing/video")
    public ResponseEntity<ApiResponse<EditingJob>> createVideoEditingJob(
            Authentication authentication,
            @RequestBody VideoEditingRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.createVideoEditingJob(user, request)));
    }

    @PostMapping("/editing/video/{jobId}/effects")
    public ResponseEntity<ApiResponse<EditingJob>> applyVideoEffects(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody List<VideoEffect> effects) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.applyVideoEffects(user, jobId, effects)));
    }

    // Photo/Image Editing
    @PostMapping("/editing/photo")
    public ResponseEntity<ApiResponse<EditingJob>> createPhotoEditingJob(
            Authentication authentication,
            @RequestBody PhotoEditingRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.createPhotoEditingJob(user, request)));
    }

    @PostMapping("/editing/photo/{jobId}/filters")
    public ResponseEntity<ApiResponse<EditingJob>> applyImageFilters(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody List<ImageFilter> filters) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.applyImageFilters(user, jobId, filters)));
    }

    // Content Templates
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<ContentTemplate>> createTemplate(
            Authentication authentication,
            @RequestBody CreateTemplateRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.createTemplate(user, request)));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ContentTemplate>>> getTemplates(
            Authentication authentication,
            @RequestParam(required = false) String templateType) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.getTemplates(user, templateType)));
    }

    @PostMapping("/templates/{templateId}/create")
    public ResponseEntity<ApiResponse<EditingJob>> createFromTemplate(
            Authentication authentication,
            @PathVariable Long templateId,
            @RequestBody CreateFromTemplateRequest request) {
        User user = (User) authentication.getPrincipal();
        request.setTemplateId(templateId);
        return ResponseEntity.ok(ApiResponse.success(mediaService.createFromTemplate(user, request)));
    }

    // Content Collaboration
    @PostMapping("/editing/{jobId}/collaborate")
    public ResponseEntity<ApiResponse<ContentCollaboration>> inviteCollaborator(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody InviteCollaboratorRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.inviteCollaborator(user, jobId, request)));
    }

    @PostMapping("/collaboration/{collaborationId}/accept")
    public ResponseEntity<ApiResponse<ContentCollaboration>> acceptCollaboration(
            Authentication authentication,
            @PathVariable Long collaborationId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.acceptCollaboration(user, collaborationId)));
    }

    // Content Versioning
    @PostMapping("/editing/{jobId}/versions")
    public ResponseEntity<ApiResponse<ContentVersion>> createVersion(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody CreateVersionRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.createVersion(user, jobId, request)));
    }

    @GetMapping("/editing/{jobId}/versions")
    public ResponseEntity<ApiResponse<List<ContentVersion>>> getVersionHistory(
            Authentication authentication,
            @PathVariable Long jobId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.getVersionHistory(user, jobId)));
    }

    @PostMapping("/editing/{jobId}/versions/{versionId}/restore")
    public ResponseEntity<ApiResponse<EditingJob>> restoreVersion(
            Authentication authentication,
            @PathVariable Long jobId,
            @PathVariable Long versionId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.restoreVersion(user, jobId, versionId)));
    }

    // Content Scheduling
    @PostMapping("/editing/{jobId}/schedule")
    public ResponseEntity<ApiResponse<EditingJob>> scheduleContent(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody ScheduleRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.scheduleContent(user, jobId, request)));
    }

    // Content Analytics
    @PostMapping("/editing/{jobId}/analytics")
    public ResponseEntity<ApiResponse<ContentAnalytics>> trackAnalytics(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody AnalyticsEvent event) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.trackAnalytics(user, jobId, event)));
    }

    @GetMapping("/editing/{jobId}/analytics")
    public ResponseEntity<ApiResponse<ContentAnalytics>> getAnalytics(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestParam(required = false, defaultValue = "7d") String timeRange) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.getAnalytics(user, jobId, timeRange)));
    }

    // Content Monetization
    @PostMapping("/editing/{jobId}/monetize")
    public ResponseEntity<ApiResponse<EditingJob>> enableMonetization(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody MonetizationSettings settings) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.enableMonetization(user, jobId, settings)));
    }

    // Content Sharing
    @PostMapping("/editing/{jobId}/share")
    public ResponseEntity<ApiResponse<ShareLink>> createShareLink(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody ShareLinkRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.createShareLink(user, jobId, request)));
    }

    // Advanced Editing
    @PostMapping("/editing/{jobId}/advanced")
    public ResponseEntity<ApiResponse<EditingJob>> applyAdvancedEditing(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestBody AdvancedEditingRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.applyAdvancedEditing(user, jobId, request)));
    }

    // Batch Processing
    @PostMapping("/editing/batch")
    public ResponseEntity<ApiResponse<EditingJob>> batchProcess(
            Authentication authentication,
            @RequestBody BatchProcessingRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(mediaService.batchProcess(user, request)));
    }

    // Job Status Updates (Internal)
    @PutMapping("/editing/jobs/{jobId}/status")
    public ResponseEntity<ApiResponse<EditingJob>> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam EditingJob.EditingJobStatus status,
            @RequestParam(required = false) String resultUri,
            @RequestParam(required = false) String errorMessage) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.updateJobStatus(jobId, status, resultUri, errorMessage)));
    }
}
