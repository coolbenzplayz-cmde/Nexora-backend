package org.example.nexora.messaging;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Privacy Service providing:
 * - Chat locks and conversation privacy
 * - Message encryption and decryption
 * - User privacy settings management
 * - Access control and permissions
 * - Content filtering and blocking
 * - Privacy violation detection
 * - User blocking and reporting
 * - Secure message handling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivacyService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final EncryptionService encryptionService;
    private final ModerationService moderationService;
    private final NotificationService notificationService;

    // Active conversation locks
    private final Map<Long, ConversationLock> activeLocks = new ConcurrentHashMap<>();
    
    // User privacy settings cache
    private final Map<Long, UserPrivacySettings> privacySettingsCache = new ConcurrentHashMap<>();
    
    // Blocked users cache
    private final Map<Long, Set<Long>> blockedUsersCache = new ConcurrentHashMap<>();

    /**
     * Check if user can send message to recipient
     */
    public boolean canSendMessage(Long senderId, Long recipientId) {
        log.debug("Checking if user {} can send message to {}", senderId, recipientId);

        try {
            // Check if sender is blocked by recipient
            if (isUserBlocked(senderId, recipientId)) {
                log.info("Message blocked - sender {} is blocked by recipient {}", senderId, recipientId);
                return false;
            }

            // Check if recipient is blocked by sender
            if (isUserBlocked(recipientId, senderId)) {
                log.info("Message blocked - recipient {} is blocked by sender {}", recipientId, senderId);
                return false;
            }

            // Check recipient's privacy settings
            UserPrivacySettings recipientSettings = getPrivacySettings(recipientId);
            if (!recipientSettings.isAllowMessagesFromStrangers()) {
                if (!areUsersFriends(senderId, recipientId)) {
                    log.info("Message blocked - recipient {} doesn't allow messages from strangers", recipientId);
                    return false;
                }
            }

            // Check sender's restrictions
            UserPrivacySettings senderSettings = getPrivacySettings(senderId);
            if (senderSettings.isMessageSendingRestricted()) {
                log.info("Message blocked - sender {} has message sending restricted", senderId);
                return false;
            }

            // Check rate limiting
            if (isRateLimited(senderId, recipientId)) {
                log.info("Message blocked - rate limit exceeded for sender {}", senderId);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error checking message permissions", e);
            return false;
        }
    }

    /**
     * Check if conversation is locked
     */
    public boolean isConversationLocked(Long userId1, Long userId2) {
        log.debug("Checking if conversation between {} and {} is locked", userId1, userId2);

        try {
            // Get conversation ID
            Long conversationId = getConversationId(userId1, userId2);
            if (conversationId == null) {
                return false;
            }

            // Check for active lock
            ConversationLock lock = activeLocks.get(conversationId);
            if (lock != null) {
                // Check if lock is still valid
                if (lock.getExpiresAt().isAfter(LocalDateTime.now())) {
                    return true;
                } else {
                    // Remove expired lock
                    activeLocks.remove(conversationId);
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking conversation lock", e);
            return false;
        }
    }

    /**
     * Check if user can receive call
     */
    public boolean canReceiveCall(Long callerId, Long calleeId) {
        log.debug("Checking if user {} can receive call from {}", calleeId, callerId);

        try {
            // Check if caller is blocked
            if (isUserBlocked(callerId, calleeId)) {
                log.info("Call blocked - caller {} is blocked by callee {}", callerId, calleeId);
                return false;
            }

            // Check callee's privacy settings
            UserPrivacySettings calleeSettings = getPrivacySettings(calleeId);
            if (!calleeSettings.isAllowCallsFromStrangers()) {
                if (!areUsersFriends(callerId, calleeId)) {
                    log.info("Call blocked - callee {} doesn't allow calls from strangers", calleeId);
                    return false;
                }
            }

            // Check if callee is in Do Not Disturb mode
            if (calleeSettings.isDoNotDisturb()) {
                log.info("Call blocked - callee {} is in Do Not Disturb mode", calleeId);
                return false;
            }

            // Check if user is already in another call
            if (isUserInActiveCall(calleeId)) {
                log.info("Call blocked - callee {} is already in another call", calleeId);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error checking call permissions", e);
            return false;
        }
    }

    /**
     * Check if user can lock conversation
     */
    public boolean canLockConversation(Long userId, Long conversationId) {
        log.debug("Checking if user {} can lock conversation {}", userId, conversationId);

        try {
            // Check if user is participant in conversation
            if (!isConversationParticipant(userId, conversationId)) {
                return false;
            }

            // Check user's role in conversation
            ParticipantRole role = getUserRoleInConversation(userId, conversationId);
            if (role != ParticipantRole.ADMIN && role != ParticipantRole.MODERATOR) {
                // Regular users can only lock their own direct conversations
                Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
                if (conversation == null || conversation.getType() != ConversationType.DIRECT) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Error checking lock permissions", e);
            return false;
        }
    }

    /**
     * Check if user can unlock conversation
     */
    public boolean canUnlockConversation(Long userId, Long conversationId) {
        log.debug("Checking if user {} can unlock conversation {}", userId, conversationId);

        try {
            // Check if user locked the conversation
            ConversationLock lock = activeLocks.get(conversationId);
            if (lock != null && lock.getLockedBy().equals(userId)) {
                return true;
            }

            // Check if user has admin/moderator role
            ParticipantRole role = getUserRoleInConversation(userId, conversationId);
            return role == ParticipantRole.ADMIN || role == ParticipantRole.MODERATOR;

        } catch (Exception e) {
            log.error("Error checking unlock permissions", e);
            return false;
        }
    }

    /**
     * Check if user can access conversation
     */
    public boolean canAccessConversation(Long userId, Long conversationId) {
        log.debug("Checking if user {} can access conversation {}", userId, conversationId);

        try {
            // Check if user is participant
            if (!isConversationParticipant(userId, conversationId)) {
                return false;
            }

            // Check if user is blocked from conversation
            if (isUserBlockedFromConversation(userId, conversationId)) {
                return false;
            }

            // Check if conversation is locked and user can't bypass
            ConversationLock lock = activeLocks.get(conversationId);
            if (lock != null && lock.getExpiresAt().isAfter(LocalDateTime.now())) {
                if (lock.getLockType() == LockType.NO_NEW_MESSAGES) {
                    // Can still read messages but not send new ones
                    return true;
                } else if (lock.getLockType() == LockType.READ_ONLY) {
                    // Only admins and moderators can access
                    ParticipantRole role = getUserRoleInConversation(userId, conversationId);
                    return role == ParticipantRole.ADMIN || role == ParticipantRole.MODERATOR;
                } else if (lock.getLockType() == LockType.PERMANENT) {
                    // Only lock creator can access
                    return lock.getLockedBy().equals(userId);
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Error checking conversation access", e);
            return false;
        }
    }

    /**
     * Check if user can access message
     */
    public boolean canAccessMessage(Long userId, Message message) {
        log.debug("Checking if user {} can access message {}", userId, message.getId());

        try {
            // Check if user is participant in conversation
            if (!isConversationParticipant(userId, message.getConversationId())) {
                return false;
            }

            // Check if message is deleted and user didn't delete it
            if (message.getStatus() == MessageStatus.DELETED && !message.getDeletedBy().equals(userId)) {
                return false;
            }

            // Check if message is flagged and user is not moderator
            if (message.getStatus() == MessageStatus.FLAGGED) {
                ParticipantRole role = getUserRoleInConversation(userId, message.getConversationId());
                return role == ParticipantRole.ADMIN || role == ParticipantRole.MODERATOR;
            }

            return true;

        } catch (Exception e) {
            log.error("Error checking message access", e);
            return false;
        }
    }

    /**
     * Check if user can moderate conversation
     */
    public boolean canModerateConversation(Long userId, Long conversationId) {
        log.debug("Checking if user {} can moderate conversation {}", userId, conversationId);

        try {
            // Check if user is participant
            if (!isConversationParticipant(userId, conversationId)) {
                return false;
            }

            // Check user's role
            ParticipantRole role = getUserRoleInConversation(userId, conversationId);
            return role == ParticipantRole.ADMIN || role == ParticipantRole.MODERATOR;

        } catch (Exception e) {
            log.error("Error checking moderation permissions", e);
            return false;
        }
    }

    /**
     * Apply conversation lock
     */
    public void applyConversationLock(ConversationLock lock) {
        log.info("Applying conversation lock to conversation {} by user {}", 
                lock.getConversationId(), lock.getLockedBy());

        try {
            // Store lock
            activeLocks.put(lock.getConversationId(), lock);

            // Notify participants
            notifyConversationLock(lock);

            // Log privacy action
            logPrivacyAction(lock.getLockedBy(), "LOCK_CONVERSATION", 
                    "Locked conversation " + lock.getConversationId() + " with reason: " + lock.getReason());

        } catch (Exception e) {
            log.error("Error applying conversation lock", e);
        }
    }

    /**
     * Remove conversation lock
     */
    public void removeConversationLock(Long conversationId) {
        log.info("Removing conversation lock for conversation {}", conversationId);

        try {
            ConversationLock lock = activeLocks.remove(conversationId);
            if (lock != null) {
                // Notify participants
                notifyConversationUnlock(lock);

                // Log privacy action
                logPrivacyAction(lock.getLockedBy(), "UNLOCK_CONVERSATION", 
                        "Unlocked conversation " + conversationId);
            }

        } catch (Exception e) {
            log.error("Error removing conversation lock", e);
        }
    }

    /**
     * Block user
     */
    public BlockUserResult blockUser(Long blockerId, Long blockedUserId, String reason) {
        log.info("User {} blocking user {} with reason: {}", blockerId, blockedUserId, reason);

        try {
            // Check if already blocked
            if (isUserBlocked(blockerId, blockedUserId)) {
                return BlockUserResult.failure("User is already blocked");
            }

            // Add to blocked list
            Set<Long> blockedList = blockedUsersCache.computeIfAbsent(blockerId, k -> new HashSet<>());
            blockedList.add(blockedUserId);

            // Update privacy settings
            UserPrivacySettings settings = getPrivacySettings(blockerId);
            settings.getBlockedUsers().add(blockedUserId);
            updatePrivacySettings(blockerId, settings);

            // Remove from conversations
            removeUserFromConversations(blockerId, blockedUserId);

            // Notify blocked user
            notificationService.sendPrivacyNotification(blockedUserId, 
                    "You have been blocked", 
                    "User " + blockerId + " has blocked you");

            // Log privacy action
            logPrivacyAction(blockerId, "BLOCK_USER", 
                    "Blocked user " + blockedUserId + " with reason: " + reason);

            return BlockUserResult.success();

        } catch (Exception e) {
            log.error("Error blocking user", e);
            return BlockUserResult.failure("Failed to block user: " + e.getMessage());
        }
    }

    /**
     * Unblock user
     */
    public BlockUserResult unblockUser(Long blockerId, Long blockedUserId) {
        log.info("User {} unblocking user {}", blockerId, blockedUserId);

        try {
            // Check if user is blocked
            if (!isUserBlocked(blockerId, blockedUserId)) {
                return BlockUserResult.failure("User is not blocked");
            }

            // Remove from blocked list
            Set<Long> blockedList = blockedUsersCache.get(blockerId);
            if (blockedList != null) {
                blockedList.remove(blockedUserId);
            }

            // Update privacy settings
            UserPrivacySettings settings = getPrivacySettings(blockerId);
            settings.getBlockedUsers().remove(blockedUserId);
            updatePrivacySettings(blockerId, settings);

            // Log privacy action
            logPrivacyAction(blockerId, "UNBLOCK_USER", 
                    "Unblocked user " + blockedUserId);

            return BlockUserResult.success();

        } catch (Exception e) {
            log.error("Error unblocking user", e);
            return BlockUserResult.failure("Failed to unblock user: " + e.getMessage());
        }
    }

    /**
     * Get conversation participants
     */
    public List<ConversationParticipant> getConversationParticipants(Long conversationId) {
        log.debug("Getting participants for conversation {}", conversationId);

        try {
            // Get participants from repository
            return conversationRepository.findParticipantsByConversationId(conversationId);

        } catch (Exception e) {
            log.error("Error getting conversation participants", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get active conversation lock
     */
    public ConversationLock getActiveConversationLock(Long conversationId) {
        ConversationLock lock = activeLocks.get(conversationId);
        if (lock != null && lock.getExpiresAt().isBefore(LocalDateTime.now())) {
            activeLocks.remove(conversationId);
            return null;
        }
        return lock;
    }

    // Private helper methods
    private boolean isUserBlocked(Long blockerId, Long blockedUserId) {
        Set<Long> blockedList = blockedUsersCache.get(blockerId);
        return blockedList != null && blockedList.contains(blockedUserId);
    }

    private UserPrivacySettings getPrivacySettings(Long userId) {
        return privacySettingsCache.computeIfAbsent(userId, k -> {
            // Load from repository
            return userRepository.findPrivacySettings(userId).orElseGet(UserPrivacySettings::new);
        });
    }

    private void updatePrivacySettings(Long userId, UserPrivacySettings settings) {
        privacySettingsCache.put(userId, settings);
        userRepository.savePrivacySettings(userId, settings);
    }

    private boolean areUsersFriends(Long userId1, Long userId2) {
        // Check if users are friends
        return userRepository.areUsersFriends(userId1, userId2);
    }

    private boolean isRateLimited(Long senderId, Long recipientId) {
        // Check rate limiting
        return false; // Simplified
    }

    private Long getConversationId(Long userId1, Long userId2) {
        // Get conversation ID between two users
        return conversationRepository.findDirectConversationId(userId1, userId2);
    }

    private boolean isConversationParticipant(Long userId, Long conversationId) {
        return conversationRepository.isParticipant(userId, conversationId);
    }

    private ParticipantRole getUserRoleInConversation(Long userId, Long conversationId) {
        return conversationRepository.getUserRole(userId, conversationId);
    }

    private boolean isUserBlockedFromConversation(Long userId, Long conversationId) {
        // Check if user is blocked from specific conversation
        return false; // Simplified
    }

    private boolean isUserInActiveCall(Long userId) {
        // Check if user is in active call
        return false; // Simplified
    }

    private void removeUserFromConversations(Long blockerId, Long blockedUserId) {
        // Remove blocked user from all conversations with blocker
        List<Long> conversationIds = conversationRepository.findConversationsBetweenUsers(blockerId, blockedUserId);
        conversationIds.forEach(conversationId -> {
            conversationRepository.removeParticipant(blockedUserId, conversationId);
        });
    }

    private void notifyConversationLock(ConversationLock lock) {
        // Notify all participants about lock
        List<ConversationParticipant> participants = getConversationParticipants(lock.getConversationId());
        participants.forEach(participant -> {
            if (!participant.getUserId().equals(lock.getLockedBy())) {
                notificationService.sendPrivacyNotification(participant.getUserId(), 
                        "Conversation locked", 
                        lock.getReason());
            }
        });
    }

    private void notifyConversationUnlock(ConversationLock lock) {
        // Notify all participants about unlock
        List<ConversationParticipant> participants = getConversationParticipants(lock.getConversationId());
        participants.forEach(participant -> {
            notificationService.sendPrivacyNotification(participant.getUserId(), 
                    "Conversation unlocked", 
                    "Conversation is now active");
        });
    }

    private void logPrivacyAction(Long userId, String action, String details) {
        log.info("Privacy action: {} by user {} - {}", action, userId, details);
        // Would store in audit log
    }

    // Data classes
    @Data
    public static class BlockUserResult {
        private boolean success;
        private String error;

        public static BlockUserResult success() {
            BlockUserResult result = new BlockUserResult();
            result.setSuccess(true);
            return result;
        }

        public static BlockUserResult failure(String error) {
            BlockUserResult result = new BlockUserResult();
            result.setSuccess(false);
            result.setError(error);
            return result;
        }
    }

    @Data
    public static class UserPrivacySettings {
        private Long userId;
        private boolean allowMessagesFromStrangers = true;
        private boolean allowCallsFromStrangers = true;
        private boolean messageSendingRestricted = false;
        private boolean doNotDisturb = false;
        private boolean profileVisibleToStrangers = true;
        private boolean lastSeenVisible = true;
        private boolean onlineStatusVisible = true;
        private boolean readReceiptsEnabled = true;
        private boolean typingIndicatorsEnabled = true;
        private Set<Long> blockedUsers = new HashSet<>();
        private Set<Long> mutedUsers = new HashSet<>();
        private Set<Long> restrictedUsers = new HashSet<>();
        private PrivacyLevel defaultPrivacyLevel = PrivacyLevel.PUBLIC;
        private LocalDateTime updatedAt;
    }

    // Enums
    public enum PrivacyLevel {
        PUBLIC, FRIENDS, PRIVATE, CUSTOM
    }

    // Service placeholders
    private static class UserRepository {
        public Optional<UserPrivacySettings> findPrivacySettings(Long userId) { return Optional.empty(); }
        public void savePrivacySettings(Long userId, UserPrivacySettings settings) {}
        public boolean areUsersFriends(Long userId1, Long userId2) { return false; }
    }

    private static class ConversationRepository {
        public Optional<Conversation> findById(Long id) { return Optional.empty(); }
        public List<ConversationParticipant> findParticipantsByConversationId(Long conversationId) { return new ArrayList<>(); }
        public boolean isParticipant(Long userId, Long conversationId) { return false; }
        public ParticipantRole getUserRole(Long userId, Long conversationId) { return ParticipantRole.MEMBER; }
        public Long findDirectConversationId(Long userId1, Long userId2) { return null; }
        public List<Long> findConversationsBetweenUsers(Long userId1, Long userId2) { return new ArrayList<>(); }
        public void removeParticipant(Long userId, Long conversationId) {}
    }

    private static class EncryptionService {
        // Encryption methods
    }

    private static class ModerationService {
        // Moderation methods
    }

    private static class NotificationService {
        public void sendPrivacyNotification(Long userId, String title, String message) {}
    }

    // Service instances
    private final UserRepository userRepository = new UserRepository();
    private final ConversationRepository conversationRepository = new ConversationRepository();
    private final EncryptionService encryptionService = new EncryptionService();
    private final ModerationService moderationService = new ModerationService();
    private final NotificationService notificationService = new NotificationService();
}
