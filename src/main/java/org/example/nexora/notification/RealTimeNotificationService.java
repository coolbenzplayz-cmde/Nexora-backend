package org.example.nexora.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.video.Video;
import org.example.nexora.social.Comment;
import org.example.nexora.social.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time Notification System that handles all user interactions:
 * - Video likes, comments, shares
 * - Follow/unfollow notifications
 * - Mentions and replies
 * - System notifications
 * - Payment and transaction alerts
 * - Content moderation alerts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    
    // In-memory notification cache for real-time delivery
    private final Map<Long, List<Notification>> notificationCache = new ConcurrentHashMap<>();
    
    // User notification preferences cache
    private final Map<Long, UserNotificationPreferences> userPreferences = new ConcurrentHashMap<>();

    /**
     * Send notification for video like
     */
    @Async
    public CompletableFuture<Void> notifyVideoLike(Long videoId, Long likerId, Long videoOwnerId) {
        if (likerId.equals(videoOwnerId)) {
            return CompletableFuture.completedFuture(null); // Don't notify self-likes
        }

        User liker = userRepository.findById(likerId).orElse(null);
        Video video = getVideoById(videoId);
        
        if (liker != null && video != null) {
            String title = "New Like!";
            String message = String.format("%s liked your video \"%s\"", liker.getUsername(), video.getTitle());
            
            Notification notification = createNotification(
                videoOwnerId, 
                title, 
                message, 
                Notification.NotificationType.LIKE,
                "VIDEO",
                videoId
            );
            
            // Add metadata for rich notifications
            notification.setMetadata(Map.of(
                "likerId", likerId.toString(),
                "likerUsername", liker.getUsername(),
                "videoTitle", video.getTitle(),
                "videoThumbnail", video.getThumbnailUrl()
            ));
            
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for video comment
     */
    @Async
    public CompletableFuture<Void> notifyVideoComment(Long videoId, Long commenterId, Long videoOwnerId, String commentContent) {
        if (commenterId.equals(videoOwnerId)) {
            return CompletableFuture.completedFuture(null); // Don't notify self-comments
        }

        User commenter = userRepository.findById(commenterId).orElse(null);
        Video video = getVideoById(videoId);
        
        if (commenter != null && video != null) {
            String title = "New Comment!";
            String message = String.format("%s commented: \"%s\"", commenter.getUsername(), 
                                         commentContent.length() > 50 ? commentContent.substring(0, 50) + "..." : commentContent);
            
            Notification notification = createNotification(
                videoOwnerId, 
                title, 
                message, 
                Notification.NotificationType.COMMENT,
                "VIDEO",
                videoId
            );
            
            notification.setMetadata(Map.of(
                "commenterId", commenterId.toString(),
                "commenterUsername", commenter.getUsername(),
                "commentContent", commentContent,
                "videoTitle", video.getTitle()
            ));
            
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for comment reply
     */
    @Async
    public CompletableFuture<Void> notifyCommentReply(Long commentId, Long replierId, Long originalCommenterId, String replyContent) {
        if (replierId.equals(originalCommenterId)) {
            return CompletableFuture.completedFuture(null); // Don't notify self-replies
        }

        User replier = userRepository.findById(replierId).orElse(null);
        Comment originalComment = getCommentById(commentId);
        
        if (replier != null && originalComment != null) {
            String title = "Reply to your comment!";
            String message = String.format("%s replied: \"%s\"", replier.getUsername(),
                                         replyContent.length() > 50 ? replyContent.substring(0, 50) + "..." : replyContent);
            
            Notification notification = createNotification(
                originalCommenterId, 
                title, 
                message, 
                Notification.NotificationType.REPLY,
                "COMMENT",
                commentId
            );
            
            notification.setMetadata(Map.of(
                "replierId", replierId.toString(),
                "replierUsername", replier.getUsername(),
                "replyContent", replyContent,
                "originalCommentId", commentId.toString()
            ));
            
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for new follower
     */
    @Async
    public CompletableFuture<Void> notifyNewFollower(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId).orElse(null);
        
        if (follower != null) {
            String title = "New Follower!";
            String message = String.format("%s started following you", follower.getUsername());
            
            Notification notification = createNotification(
                followingId, 
                title, 
                message, 
                Notification.NotificationType.FOLLOW,
                "USER",
                followerId
            );
            
            notification.setMetadata(Map.of(
                "followerId", followerId.toString(),
                "followerUsername", follower.getUsername(),
                "followerAvatar", follower.getProfilePictureUrl(),
                "followerVerified", follower.getIsCreatorVerified().toString()
            ));
            
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for video mention
     */
    @Async
    public CompletableFuture<Void> notifyVideoMention(Long videoId, Long mentionerId, Long mentionedUserId, String mentionContext) {
        User mentioner = userRepository.findById(mentionerId).orElse(null);
        Video video = getVideoById(videoId);
        
        if (mentioner != null && video != null) {
            String title = "You were mentioned!";
            String message = String.format("%s mentioned you in \"%s\"", mentioner.getUsername(), video.getTitle());
            
            Notification notification = createNotification(
                mentionedUserId, 
                title, 
                message, 
                Notification.NotificationType.MENTION,
                "VIDEO",
                videoId
            );
            
            notification.setMetadata(Map.of(
                "mentionerId", mentionerId.toString(),
                "mentionerUsername", mentioner.getUsername(),
                "videoTitle", video.getTitle(),
                "mentionContext", mentionContext
            ));
            
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for payment received
     */
    @Async
    public CompletableFuture<Void> notifyPaymentReceived(Long receiverId, Long senderId, Double amount, String reference) {
        User sender = userRepository.findById(senderId).orElse(null);
        
        if (sender != null) {
            String title = "Payment Received!";
            String message = String.format("You received $%.2f from %s", amount, sender.getUsername());
            
            Notification notification = createNotification(
                receiverId, 
                title, 
                message, 
                Notification.NotificationType.PAYMENT,
                "TRANSACTION",
                null
            );
            
            notification.setMetadata(Map.of(
                "senderId", senderId.toString(),
                "senderUsername", sender.getUsername(),
                "amount", amount.toString(),
                "reference", reference
            ));
            
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for content moderation
     */
    @Async
    public CompletableFuture<Void> notifyContentModeration(Long userId, String contentType, Long contentId, String action, String reason) {
        String title = String.format("Content %s", action.toLowerCase());
        String message = String.format("Your %s was %s: %s", contentType.toLowerCase(), action.toLowerCase(), reason);
        
        Notification notification = createNotification(
            userId, 
            title, 
            message, 
            Notification.NotificationType.MODERATION,
            contentType.toUpperCase(),
            contentId
        );
        
        notification.setPriority(Notification.NotificationPriority.HIGH);
        notification.setMetadata(Map.of(
            "contentType", contentType,
            "contentId", contentId.toString(),
            "action", action,
            "reason", reason
        ));
        
        deliverRealTimeNotification(notification);
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for system announcements
     */
    @Async
    public CompletableFuture<Void> notifySystemAnnouncement(String title, String message, Notification.NotificationPriority priority) {
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            Notification notification = createNotification(
                user.getId(), 
                title, 
                message, 
                Notification.NotificationType.SYSTEM,
                "SYSTEM",
                null
            );
            
            notification.setPriority(priority);
            deliverRealTimeNotification(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send notification for achievement unlocked
     */
    @Async
    public CompletableFuture<Void> notifyAchievementUnlocked(Long userId, String achievementName, String description) {
        String title = "Achievement Unlocked!";
        String message = String.format("You earned the \"%s\" achievement: %s", achievementName, description);
        
        Notification notification = createNotification(
            userId, 
            title, 
            message, 
            Notification.NotificationType.ACHIEVEMENT,
            "ACHIEVEMENT",
            null
        );
        
        notification.setPriority(Notification.NotificationPriority.MEDIUM);
        notification.setMetadata(Map.of(
            "achievementName", achievementName,
            "achievementDescription", description
        ));
        
        deliverRealTimeNotification(notification);
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Create and save notification
     */
    private Notification createNotification(Long userId, String title, String message, 
                                          Notification.NotificationType type, String referenceType, Long referenceId) {
        // Check user notification preferences
        if (!shouldSendNotification(userId, type)) {
            return null;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setPriority(Notification.NotificationPriority.MEDIUM);
        
        return notificationRepository.save(notification);
    }

    /**
     * Deliver notification in real-time via WebSocket
     */
    private void deliverRealTimeNotification(Notification notification) {
        if (notification == null) return;
        
        try {
            // Add to cache
            notificationCache.computeIfAbsent(notification.getUserId(), k -> new ArrayList<>()).add(notification);
            
            // Send via WebSocket
            webSocketHandler.sendNotification(notification.getUserId(), notification);
            
            // Send push notification if user is offline (would integrate with FCM/APNS)
            if (isUserOffline(notification.getUserId())) {
                sendPushNotification(notification);
            }
            
            log.info("Delivered real-time notification {} to user {}", notification.getId(), notification.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to deliver notification {} to user {}", notification.getId(), notification.getUserId(), e);
        }
    }

    /**
     * Get user notifications with pagination
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("Not authorized to modify this notification");
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        
        // Clear from cache
        notificationCache.remove(userId);
    }

    /**
     * Get unread count for a user
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("Not authorized to delete this notification");
        }
        
        notificationRepository.delete(notification);
    }

    /**
     * Update user notification preferences
     */
    @Transactional
    public void updateNotificationPreferences(Long userId, UserNotificationPreferences preferences) {
        userPreferences.put(userId, preferences);
        // In a real implementation, this would be saved to database
    }

    /**
     * Check if notification should be sent based on user preferences
     */
    private boolean shouldSendNotification(Long userId, Notification.NotificationType type) {
        UserNotificationPreferences preferences = userPreferences.get(userId);
        if (preferences == null) {
            return true; // Default to sending if no preferences set
        }
        
        return preferences.isEnabled(type);
    }

    /**
     * Check if user is offline
     */
    private boolean isUserOffline(Long userId) {
        // This would check WebSocket connection status
        return !webSocketHandler.isUserConnected(userId);
    }

    /**
     * Send push notification (would integrate with FCM/APNS)
     */
    private void sendPushNotification(Notification notification) {
        // Placeholder for push notification integration
        log.info("Would send push notification {} to user {}", notification.getId(), notification.getUserId());
    }

    // Helper methods to get entities (would use actual repositories)
    private Video getVideoById(Long videoId) {
        // Placeholder implementation
        return null;
    }

    private Comment getCommentById(Long commentId) {
        // Placeholder implementation
        return null;
    }

    /**
     * Clean up old notifications
     */
    @Transactional
    public void cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);
        
        for (Notification notification : oldNotifications) {
            if (notification.getIsRead()) {
                notificationRepository.delete(notification);
            }
        }
        
        log.info("Cleaned up {} old notifications", oldNotifications.size());
    }

    /**
     * Get notification statistics for a user
     */
    public NotificationStatistics getNotificationStatistics(Long userId) {
        long totalNotifications = notificationRepository.countByUserId(userId);
        long unreadNotifications = getUnreadCount(userId);
        
        Map<Notification.NotificationType, Long> typeBreakdown = notificationRepository.countByUserIdAndType(userId);
        
        return NotificationStatistics.builder()
                .totalNotifications(totalNotifications)
                .unreadNotifications(unreadNotifications)
                .readNotifications(totalNotifications - unreadNotifications)
                .typeBreakdown(typeBreakdown)
                .build();
    }
}
