package org.example.nexora.user;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * User Service Controller - Handles user management endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserServiceController {

    private final UserProfileService userProfileService;
    private final UserSearchService userSearchService;
    private final UserRelationshipService userRelationshipService;
    private final UserActivityService userActivityService;
    private final UserAnalyticsService userAnalyticsService;
    private final UserPrivacyService userPrivacyService;

    /**
     * Get user profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Long userId,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Getting user profile: {} by user: {}", userId, currentUserId);

        try {
            // Check privacy permissions
            if (!userPrivacyService.canViewProfile(currentUserId, userId)) {
                return ResponseEntity.badRequest()
                        .body(UserProfileResponse.failure("Access denied"));
            }

            UserProfile profile = userProfileService.getUserProfile(userId);
            
            // Apply privacy filters
            UserProfile filteredProfile = userPrivacyService.filterProfileData(profile, currentUserId);

            UserProfileResponse response = new UserProfileResponse();
            response.setSuccess(true);
            response.setProfile(filteredProfile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get user profile", e);
            return ResponseEntity.badRequest()
                    .body(UserProfileResponse.failure("Failed to get profile: " + e.getMessage()));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Updating user profile: {} by user: {}", userId, currentUserId);

        try {
            // Check ownership
            if (!userId.equals(currentUserId)) {
                return ResponseEntity.badRequest()
                        .body(UserProfileResponse.failure("Access denied"));
            }

            UserProfile updatedProfile = userProfileService.updateUserProfile(userId, request);

            UserProfileResponse response = new UserProfileResponse();
            response.setSuccess(true);
            response.setProfile(updatedProfile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to update user profile", e);
            return ResponseEntity.badRequest()
                    .body(UserProfileResponse.failure("Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filters,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Searching users: {} by user: {}", query, currentUserId);

        try {
            UserSearchRequest searchRequest = new UserSearchRequest();
            searchRequest.setQuery(query);
            searchRequest.setPage(page);
            searchRequest.setSize(size);
            searchRequest.setFilters(filters);

            UserSearchResult searchResult = userSearchService.searchUsers(searchRequest, currentUserId);

            UserSearchResponse response = new UserSearchResponse();
            response.setSuccess(true);
            response.setUsers(searchResult.getUsers());
            response.setTotal(searchResult.getTotal());
            response.setPage(searchResult.getPage());
            response.setSize(searchResult.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to search users", e);
            return ResponseEntity.badRequest()
                    .body(UserSearchResponse.failure("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Get user relationships
     */
    @GetMapping("/{userId}/relationships")
    public ResponseEntity<UserRelationshipsResponse> getUserRelationships(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Getting user relationships: {} type: {} by user: {}", userId, type, currentUserId);

        try {
            // Check privacy permissions
            if (!userPrivacyService.canViewRelationships(currentUserId, userId)) {
                return ResponseEntity.badRequest()
                        .body(UserRelationshipsResponse.failure("Access denied"));
            }

            UserRelationshipsResult result = userRelationshipService.getUserRelationships(
                    userId, type, page, size, currentUserId);

            UserRelationshipsResponse response = new UserRelationshipsResponse();
            response.setSuccess(true);
            response.setRelationships(result.getRelationships());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get user relationships", e);
            return ResponseEntity.badRequest()
                    .body(UserRelationshipsResponse.failure("Failed to get relationships: " + e.getMessage()));
        }
    }

    /**
     * Follow user
     */
    @PostMapping("/{userId}/follow")
    public ResponseEntity<FollowResponse> followUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("User {} following user: {}", currentUserId, userId);

        try {
            // Check if already following
            if (userRelationshipService.isFollowing(currentUserId, userId)) {
                return ResponseEntity.badRequest()
                        .body(FollowResponse.failure("Already following this user"));
            }

            // Create follow relationship
            UserRelationship relationship = userRelationshipService.createFollowRelationship(currentUserId, userId);

            // Send notification
            // notificationService.sendFollowNotification(userId, currentUserId);

            FollowResponse response = new FollowResponse();
            response.setSuccess(true);
            response.setRelationship(relationship);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to follow user", e);
            return ResponseEntity.badRequest()
                    .body(FollowResponse.failure("Failed to follow user: " + e.getMessage()));
        }
    }

    /**
     * Unfollow user
     */
    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<UnfollowResponse> unfollowUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("User {} unfollowing user: {}", currentUserId, userId);

        try {
            // Check if following
            if (!userRelationshipService.isFollowing(currentUserId, userId)) {
                return ResponseEntity.badRequest()
                        .body(UnfollowResponse.failure("Not following this user"));
            }

            // Remove follow relationship
            userRelationshipService.removeFollowRelationship(currentUserId, userId);

            UnfollowResponse response = new UnfollowResponse();
            response.setSuccess(true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to unfollow user", e);
            return ResponseEntity.badRequest()
                    .body(UnfollowResponse.failure("Failed to unfollow user: " + e.getMessage()));
        }
    }

    /**
     * Get user activity
     */
    @GetMapping("/{userId}/activity")
    public ResponseEntity<UserActivityResponse> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Getting user activity: {} type: {} by user: {}", userId, type, currentUserId);

        try {
            // Check privacy permissions
            if (!userPrivacyService.canViewActivity(currentUserId, userId)) {
                return ResponseEntity.badRequest()
                        .body(UserActivityResponse.failure("Access denied"));
            }

            UserActivityResult result = userActivityService.getUserActivity(userId, type, page, size, currentUserId);

            UserActivityResponse response = new UserActivityResponse();
            response.setSuccess(true);
            response.setActivities(result.getActivities());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get user activity", e);
            return ResponseEntity.badRequest()
                    .body(UserActivityResponse.failure("Failed to get activity: " + e.getMessage()));
        }
    }

    /**
     * Get user analytics
     */
    @GetMapping("/{userId}/analytics")
    public ResponseEntity<UserAnalyticsResponse> getUserAnalytics(
            @PathVariable Long userId,
            @RequestParam(required = false) String period,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Getting user analytics: {} period: {} by user: {}", userId, period, currentUserId);

        try {
            // Check ownership or admin permissions
            if (!userId.equals(currentUserId) && !userPrivacyService.hasAdminAccess(currentUserId)) {
                return ResponseEntity.badRequest()
                        .body(UserAnalyticsResponse.failure("Access denied"));
            }

            UserAnalytics analytics = userAnalyticsService.getUserAnalytics(userId, period);

            UserAnalyticsResponse response = new UserAnalyticsResponse();
            response.setSuccess(true);
            response.setAnalytics(analytics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get user analytics", e);
            return ResponseEntity.badRequest()
                    .body(UserAnalyticsResponse.failure("Failed to get analytics: " + e.getMessage()));
        }
    }

    /**
     * Update user privacy settings
     */
    @PutMapping("/{userId}/privacy")
    public ResponseEntity<PrivacySettingsResponse> updatePrivacySettings(
            @PathVariable Long userId,
            @Valid @RequestBody PrivacySettingsRequest request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        log.info("Updating privacy settings for user: {} by user: {}", userId, currentUserId);

        try {
            // Check ownership
            if (!userId.equals(currentUserId)) {
                return ResponseEntity.badRequest()
                        .body(PrivacySettingsResponse.failure("Access denied"));
            }

            PrivacySettings updatedSettings = userPrivacyService.updatePrivacySettings(userId, request);

            PrivacySettingsResponse response = new PrivacySettingsResponse();
            response.setSuccess(true);
            response.setSettings(updatedSettings);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to update privacy settings", e);
            return ResponseEntity.badRequest()
                    .body(PrivacySettingsResponse.failure("Failed to update privacy settings: " + e.getMessage()));
        }
    }

    // Request classes
    @Data
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String bio;
        private String avatar;
        private String coverImage;
        private String location;
        private String website;
        private java.util.Map<String, Object> customFields;
    }

    @Data
    public static class UserSearchRequest {
        private String query;
        private int page;
        private int size;
        private String filters;
    }

    @Data
    public static class PrivacySettingsRequest {
        private boolean profilePublic;
        private boolean allowMessages;
        private boolean showActivity;
        private boolean showRelationships;
        private java.util.List<String> blockedUsers;
        private java.util.List<String> mutedUsers;
    }

    // Response classes
    @Data
    public static class UserProfileResponse {
        private boolean success;
        private UserProfile profile;
        private String error;

        public static UserProfileResponse failure(String error) {
            UserProfileResponse response = new UserProfileResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class UserSearchResponse {
        private boolean success;
        private java.util.List<UserProfile> users;
        private long total;
        private int page;
        private int size;
        private String error;

        public static UserSearchResponse failure(String error) {
            UserSearchResponse response = new UserSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class UserRelationshipsResponse {
        private boolean success;
        private java.util.List<UserRelationship> relationships;
        private long total;
        private int page;
        private int size;
        private String error;

        public static UserRelationshipsResponse failure(String error) {
            UserRelationshipsResponse response = new UserRelationshipsResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class FollowResponse {
        private boolean success;
        private UserRelationship relationship;
        private String error;

        public static FollowResponse failure(String error) {
            FollowResponse response = new FollowResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class UnfollowResponse {
        private boolean success;
        private String error;

        public static UnfollowResponse failure(String error) {
            UnfollowResponse response = new UnfollowResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class UserActivityResponse {
        private boolean success;
        private java.util.List<UserActivity> activities;
        private long total;
        private int page;
        private int size;
        private String error;

        public static UserActivityResponse failure(String error) {
            UserActivityResponse response = new UserActivityResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class UserAnalyticsResponse {
        private boolean success;
        private UserAnalytics analytics;
        private String error;

        public static UserAnalyticsResponse failure(String error) {
            UserAnalyticsResponse response = new UserAnalyticsResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class PrivacySettingsResponse {
        private boolean success;
        private PrivacySettings settings;
        private String error;

        public static PrivacySettingsResponse failure(String error) {
            PrivacySettingsResponse response = new PrivacySettingsResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }
}

// Service placeholders
class UserProfileService {
    public UserProfile getUserProfile(Long userId) { return new UserProfile(); }
    public UserProfile updateUserProfile(Long userId, UpdateProfileRequest request) { return new UserProfile(); }
}

class UserSearchService {
    public UserSearchResult searchUsers(UserSearchRequest request, Long currentUserId) { return new UserSearchResult(); }
}

class UserRelationshipService {
    public UserRelationshipsResult getUserRelationships(Long userId, String type, int page, int size, Long currentUserId) { return new UserRelationshipsResult(); }
    public boolean isFollowing(Long followerId, Long followingId) { return false; }
    public UserRelationship createFollowRelationship(Long followerId, Long followingId) { return new UserRelationship(); }
    public void removeFollowRelationship(Long followerId, Long followingId) {}
}

class UserActivityService {
    public UserActivityResult getUserActivity(Long userId, String type, int page, int size, Long currentUserId) { return new UserActivityResult(); }
}

class UserAnalyticsService {
    public UserAnalytics getUserAnalytics(Long userId, String period) { return new UserAnalytics(); }
}

class UserPrivacyService {
    public boolean canViewProfile(Long currentUserId, Long targetUserId) { return true; }
    public UserProfile filterProfileData(UserProfile profile, Long currentUserId) { return profile; }
    public boolean canViewRelationships(Long currentUserId, Long targetUserId) { return true; }
    public boolean canViewActivity(Long currentUserId, Long targetUserId) { return true; }
    public boolean hasAdminAccess(Long userId) { return false; }
    public PrivacySettings updatePrivacySettings(Long userId, PrivacySettingsRequest request) { return new PrivacySettings(); }
}

// Data classes
class UserProfile {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatar;
    private String coverImage;
    private String location;
    private String website;
    private java.util.Map<String, Object> customFields;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}

class UserSearchResult {
    private java.util.List<UserProfile> users;
    private long total;
    private int page;
    private int size;
}

class UserRelationship {
    private Long relationshipId;
    private Long userId;
    private Long relatedUserId;
    private String type;
    private java.time.LocalDateTime createdAt;
}

class UserRelationshipsResult {
    private java.util.List<UserRelationship> relationships;
    private long total;
    private int page;
    private int size;
}

class UserActivity {
    private Long activityId;
    private Long userId;
    private String type;
    private String description;
    private java.time.LocalDateTime createdAt;
}

class UserActivityResult {
    private java.util.List<UserActivity> activities;
    private long total;
    private int page;
    private int size;
}

class UserAnalytics {
    private Long userId;
    private java.util.Map<String, Object> metrics;
    private java.time.LocalDateTime generatedAt;
}

class PrivacySettings {
    private Long userId;
    private boolean profilePublic;
    private boolean allowMessages;
    private boolean showActivity;
    private boolean showRelationships;
    private java.util.List<String> blockedUsers;
    private java.util.List<String> mutedUsers;
}
