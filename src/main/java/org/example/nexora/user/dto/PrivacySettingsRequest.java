package org.example.nexora.user.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Privacy settings request
 */
@Data
public class PrivacySettingsRequest {
    
    private boolean profilePublic = true;
    private boolean showEmail = false;
    private boolean showPhone = false;
    private boolean showLocation = false;
    private boolean allowMessagesFromStrangers = true;
    private boolean allowCallsFromStrangers = true;
    private boolean allowFriendRequests = true;
    private boolean showOnlineStatus = true;
    private boolean showLastSeen = true;
    private String searchVisibility; // PUBLIC, FRIENDS_ONLY, PRIVATE
    private String postVisibility; // PUBLIC, FRIENDS_ONLY, PRIVATE
    private String[] blockedUsers;
    
    public PrivacySettingsRequest() {
        this.searchVisibility = "PUBLIC";
        this.postVisibility = "FRIENDS_ONLY";
        this.blockedUsers = new String[0];
    }
}
