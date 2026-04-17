package org.example.nexora.media;

import org.example.nexora.common.ApiResponse;
import org.example.nexora.media.dto.CreateEditingJobRequest;
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
}
