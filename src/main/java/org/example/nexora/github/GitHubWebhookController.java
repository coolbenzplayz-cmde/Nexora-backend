package org.example.nexora.github;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * GitHub Webhook Controller
 * Handles incoming webhook events from GitHub
 * 
 * To set up:
 * 1. Go to your GitHub repository Settings > Webhooks
 * 2. Add webhook URL: https://your-backend-url/api/webhooks/github
 * 3. Select events you want to receive (issues, pull_requests, push, etc.)
 * 4. Set a secret token for security (configure in application properties)
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class GitHubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);
    
    private final GitHubWebhookService webhookService;

    public GitHubWebhookController(GitHubWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * GitHub webhook endpoint
     * Receives events like: push, issues, pull_request, release, etc.
     */
    @PostMapping("/github")
    public ResponseEntity<Map<String, String>> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload) {
        
        log.info("Received GitHub webhook: Event type = {}", eventType);
        
        try {
            // Process webhook event
            webhookService.processWebhookEvent(eventType, signature, payload);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Webhook processed successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error processing GitHub webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error", 
                    "message", "Failed to process webhook"
                ));
        }
    }
}
