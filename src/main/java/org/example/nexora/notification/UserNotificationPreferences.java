package org.example.nexora.notification;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

/**
 * User notification preferences
 */
@Data
public class UserNotificationPreferences {
    
    private Long userId;
    private boolean emailEnabled = true;
    private boolean pushEnabled = true;
    private boolean smsEnabled = false;
    private Map<String, Boolean> categoryPreferences = new HashMap<>();
    private String frequency; // IMMEDIATE, HOURLY, DAILY, WEEKLY
    
    public UserNotificationPreferences() {
        // Default category preferences
        categoryPreferences.put("comments", true);
        categoryPreferences.put("likes", true);
        categoryPreferences.put("follows", true);
        categoryPreferences.put("mentions", true);
        categoryPreferences.put("system", true);
        this.frequency = "IMMEDIATE";
    }
}
