package org.example.nexora.messaging;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Advanced Messaging Controller providing:
 * - Voice message recording and playback
 * - Video calling with WebRTC
 * - Chat locks and privacy controls
 * - End-to-end encryption
 * - Message reactions and replies
 * - Rich media sharing
 * - Real-time communication
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class AdvancedMessagingController {

    private final AdvancedMessagingService messagingService;
    private final VoiceMessageService voiceMessageService;
    private final VideoCallService videoCallService;
    private final PrivacyService privacyService;
    private final EncryptionService encryptionService;

    /**
     * Send text message
     */
    @PostMapping("/messages/text")
    public ResponseEntity<AdvancedMessagingService.MessageSendResult> sendTextMessage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AdvancedMessagingService.TextMessageRequest request) {
        request.setSenderId(userId);
        AdvancedMessagingService.MessageSendResult result = messagingService.sendTextMessage(request);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Send voice message
     */
    @PostMapping("/messages/voice")
    public ResponseEntity<AdvancedMessagingService.MessageSendResult> sendVoiceMessage(
            @AuthenticationPrincipal Long userId,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) Long replyToMessageId) {
        
        try {
            AdvancedMessagingService.VoiceMessageRequest request = new AdvancedMessagingService.VoiceMessageRequest();
            request.setSenderId(userId);
            request.setRecipientId(recipientId);
            request.setConversationId(conversationId);
            request.setVoiceData(audioFile.getBytes());
            request.setAudioFormat(audioFile.getContentType());
            request.setReplyToMessageId(replyToMessageId);
            
            AdvancedMessagingService.MessageSendResult result = messagingService.sendVoiceMessage(request);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("Failed to send voice message", e);
            return ResponseEntity.badRequest().body(
                AdvancedMessagingService.MessageSendResult.failure("Failed to send voice message"));
        }
    }

    /**
     * Start voice recording
     */
    @PostMapping("/voice/recording/start")
    public ResponseEntity<VoiceMessageService.VoiceRecordingSession> startVoiceRecording(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VoiceRecordingStartRequest request) {
        
        VoiceMessageService.RecordingOptions options = new VoiceMessageService.RecordingOptions();
        options.setMaxDuration(request.getMaxDuration());
        options.setFormat(request.getFormat());
        options.setQuality(request.getQuality());
        options.setRealTimeEffects(request.isRealTimeEffects());
        options.setEffects(request.getEffects());
        options.setNoiseReduction(request.isNoiseReduction());
        options.setEchoCancellation(request.isEchoCancellation());
        
        VoiceMessageService.VoiceRecordingSession session = voiceMessageService.startVoiceRecording(userId, options).join();
        
        return ResponseEntity.ok(session);
    }

    /**
     * Add audio chunk to recording
     */
    @PostMapping("/voice/recording/{sessionId}/chunk")
    public ResponseEntity<VoiceMessageService.VoiceRecordingResult> addAudioChunk(
            @PathVariable String sessionId,
            @RequestParam("audio") MultipartFile audioChunk) {
        
        VoiceMessageService.VoiceRecordingResult result = voiceMessageService.addAudioChunk(
                sessionId, audioChunk.getBytes());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Stop voice recording
     */
    @PostMapping("/voice/recording/{sessionId}/stop")
    public ResponseEntity<VoiceMessageService.VoiceMessageProcessed> stopVoiceRecording(
            @PathVariable String sessionId) {
        
        VoiceMessageService.VoiceMessageProcessed result = voiceMessageService.stopVoiceRecording(sessionId).join();
        return ResponseEntity.ok(result);
    }

    /**
     * Get voice message for playback
     */
    @GetMapping("/voice/playback")
    public ResponseEntity<VoiceMessageService.VoicePlaybackResult> getVoiceMessageForPlayback(
            @AuthenticationPrincipal Long userId,
            @RequestParam String audioUrl,
            @RequestParam String playbackToken) {
        
        VoiceMessageService.VoicePlaybackResult result = voiceMessageService.getVoiceMessageForPlayback(
                audioUrl, userId, playbackToken);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Apply voice effects
     */
    @PostMapping("/voice/effects")
    public ResponseEntity<VoiceMessageService.VoiceEffectsResult> applyVoiceEffects(
            @AuthenticationPrincipal Long userId,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam List<VoiceMessageService.VoiceEffect> effects) {
        
        VoiceMessageService.VoiceEffectsResult result = voiceMessageService.applyVoiceEffects(
                audioFile.getBytes(), effects);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get voice message analytics
     */
    @GetMapping("/voice/analytics/{messageId}")
    public ResponseEntity<VoiceMessageService.VoiceMessageAnalytics> getVoiceMessageAnalytics(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long messageId) {
        
        VoiceMessageService.VoiceMessageAnalytics analytics = voiceMessageService.getVoiceMessageAnalytics(messageId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Initiate video call
     */
    @PostMapping("/calls/initiate")
    public ResponseEntity<AdvancedMessagingService.VideoCallResult> initiateVideoCall(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AdvancedMessagingService.VideoCallRequest request) {
        
        request.setCallerId(userId);
        AdvancedMessagingService.VideoCallResult result = messagingService.initiateVideoCall(request);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Answer video call
     */
    @PostMapping("/calls/{callId}/answer")
    public ResponseEntity<AdvancedMessagingService.VideoCallResult> answerVideoCall(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId,
            @RequestParam boolean accept) {
        
        AdvancedMessagingService.VideoCallResult result = messagingService.answerVideoCall(callId, userId, accept);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * End video call
     */
    @PostMapping("/calls/{callId}/end")
    public ResponseEntity<AdvancedMessagingService.VideoCallResult> endVideoCall(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId) {
        
        AdvancedMessagingService.VideoCallResult result = messagingService.endVideoCall(callId, userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Initiate peer connection
     */
    @PostMapping("/calls/{callId}/peer-connection")
    public ResponseEntity<VideoCallService.PeerConnectionResult> initiatePeerConnection(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId,
            @Valid @RequestBody VideoCallService.PeerConnectionConfig config) {
        
        VideoCallService.PeerConnectionResult result = videoCallService.initiatePeerConnection(callId, userId, config).join();
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Handle SDP answer
     */
    @PostMapping("/calls/peer-connection/{connectionId}/sdp-answer")
    public ResponseEntity<VideoCallService.PeerConnectionResult> handleSDPAnswer(
            @PathVariable String connectionId,
            @RequestBody String sdpAnswer) {
        
        VideoCallService.PeerConnectionResult result = videoCallService.handleSDPAnswer(connectionId, sdpAnswer);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Handle ICE candidate
     */
    @PostMapping("/calls/peer-connection/{connectionId}/ice-candidate")
    public ResponseEntity<VideoCallService.PeerConnectionResult> handleICECandidate(
            @PathVariable String connectionId,
            @RequestBody ICECandidate candidate) {
        
        VideoCallService.PeerConnectionResult result = videoCallService.handleICECandidate(connectionId, candidate);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Start screen sharing
     */
    @PostMapping("/calls/{callId}/screen-share/start")
    public ResponseEntity<VideoCallService.ScreenShareResult> startScreenSharing(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId,
            @Valid @RequestBody VideoCallService.ScreenShareConfig config) {
        
        VideoCallService.ScreenShareResult result = videoCallService.startScreenSharing(callId, userId, config);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Stop screen sharing
     */
    @PostMapping("/calls/{callId}/screen-share/stop")
    public ResponseEntity<VideoCallService.ScreenShareResult> stopScreenSharing(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId) {
        
        VideoCallService.ScreenShareResult result = videoCallService.stopScreenSharing(callId, userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Start call recording
     */
    @PostMapping("/calls/{callId}/recording/start")
    public ResponseEntity<VideoCallService.CallRecordingResult> startCallRecording(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId,
            @Valid @RequestBody VideoCallService.RecordingConfig config) {
        
        VideoCallService.CallRecordingResult result = videoCallService.startCallRecording(callId, config);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Stop call recording
     */
    @PostMapping("/calls/{callId}/recording/stop")
    public ResponseEntity<VideoCallService.CallRecordingResult> stopCallRecording(
            @PathVariable String callId) {
        
        VideoCallService.CallRecordingResult result = videoCallService.stopCallRecording(callId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Optimize video quality
     */
    @PostMapping("/calls/{callId}/optimize-quality")
    public ResponseEntity<VideoCallService.QualityOptimizationResult> optimizeVideoQuality(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId) {
        
        VideoCallService.QualityOptimizationResult result = videoCallService.optimizeVideoQuality(callId, userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get call analytics
     */
    @GetMapping("/calls/{callId}/analytics")
    public ResponseEntity<VideoCallService.CallAnalytics> getCallAnalytics(
            @AuthenticationPrincipal Long userId,
            @PathVariable String callId) {
        
        VideoCallService.CallAnalytics analytics = videoCallService.getCallAnalytics(callId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Lock conversation
     */
    @PostMapping("/conversations/{conversationId}/lock")
    public ResponseEntity<AdvancedMessagingService.ConversationLockResult> lockConversation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody AdvancedMessagingService.LockRequest request) {
        
        AdvancedMessagingService.ConversationLockResult result = messagingService.lockConversation(
                userId, conversationId, request);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Unlock conversation
     */
    @PostMapping("/conversations/{conversationId}/unlock")
    public ResponseEntity<AdvancedMessagingService.ConversationLockResult> unlockConversation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId) {
        
        AdvancedMessagingService.ConversationLockResult result = messagingService.unlockConversation(
                userId, conversationId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get conversation
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<AdvancedMessagingService.ConversationResult> getConversation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        AdvancedMessagingService.ConversationResult result = messagingService.getConversation(
                userId, conversationId, page, size);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Add message reaction
     */
    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<AdvancedMessagingService.MessageReactionResult> addMessageReaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long messageId,
            @RequestParam String reaction) {
        
        AdvancedMessagingService.MessageReactionResult result = messagingService.addMessageReaction(
                userId, messageId, reaction);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Delete message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<AdvancedMessagingService.MessageDeleteResult> deleteMessage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long messageId) {
        
        AdvancedMessagingService.MessageDeleteResult result = messagingService.deleteMessage(userId, messageId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Block user
     */
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<PrivacyService.BlockUserResult> blockUser(
            @AuthenticationPrincipal Long blockerId,
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        
        PrivacyService.BlockUserResult result = privacyService.blockUser(blockerId, userId, reason);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Unblock user
     */
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<PrivacyService.BlockUserResult> unblockUser(
            @AuthenticationPrincipal Long blockerId,
            @PathVariable Long userId) {
        
        PrivacyService.BlockUserResult result = privacyService.unblockUser(blockerId, userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Generate encryption keys
     */
    @PostMapping("/encryption/keys/generate")
    public ResponseEntity<EncryptionService.KeyPairResult> generateEncryptionKeys(
            @AuthenticationPrincipal Long userId) {
        
        EncryptionService.KeyPairResult result = encryptionService.generateUserKeyPair(userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Rotate encryption keys
     */
    @PostMapping("/encryption/keys/rotate")
    public ResponseEntity<EncryptionService.KeyRotationResult> rotateEncryptionKeys(
            @AuthenticationPrincipal Long userId) {
        
        EncryptionService.KeyRotationResult result = encryptionService.rotateUserKeys(userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Revoke encryption keys
     */
    @PostMapping("/encryption/keys/revoke")
    public ResponseEntity<EncryptionService.KeyRevocationResult> revokeEncryptionKeys(
            @AuthenticationPrincipal Long userId) {
        
        EncryptionService.KeyRevocationResult result = encryptionService.revokeUserKeys(userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // Request classes
    @Data
    public static class VoiceRecordingStartRequest {
        private java.time.Duration maxDuration;
        private VoiceMessageService.AudioFormat format;
        private VoiceMessageService.AudioQuality quality;
        private boolean realTimeEffects;
        private List<VoiceMessageService.VoiceEffect> effects;
        private boolean noiseReduction;
        private boolean echoCancellation;
    }
}
