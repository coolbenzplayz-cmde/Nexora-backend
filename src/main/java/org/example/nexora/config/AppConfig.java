package org.example.nexora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {

    @Value("${app.name:Nexora}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Digital Super-App Ecosystem}")
    private String appDescription;

    @Value("${app.features.social:true}")
    private boolean socialEnabled;

    @Value("${app.features.messaging:true}")
    private boolean messagingEnabled;

    @Value("${app.features.marketplace:true}")
    private boolean marketplaceEnabled;

    @Value("${app.features.foodDelivery:true}")
    private boolean foodDeliveryEnabled;

    @Value("${app.features.rideHailing:true}")
    private boolean rideHailingEnabled;

    @Value("${app.features.groceryDelivery:true}")
    private boolean groceryDeliveryEnabled;

    @Value("${app.features.video:true}")
    private boolean videoEnabled;

    @Value("${app.features.advertising:true}")
    private boolean advertisingEnabled;

    @Value("${app.features.ai:true}")
    private boolean aiEnabled;

    @Value("${app.features.wallet:true}")
    private boolean walletEnabled;

    @Value("${app.features.admin:true}")
    private boolean adminEnabled;

    @Value("${app.rate-limit.requests:100}")
    private int rateLimitRequests;

    @Value("${app.rate-limit.window:60}")
    private int rateLimitWindow;

    @Value("${app.upload.max-file-size:10485760}")
    private long maxFileSize;

    @Value("${app.upload.max-image-size:5242880}")
    private long maxImageSize;

    @Value("${app.upload.max-video-size:104857600}")
    private long maxVideoSize;

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public boolean isSocialEnabled() {
        return socialEnabled;
    }

    public boolean isMessagingEnabled() {
        return messagingEnabled;
    }

    public boolean isMarketplaceEnabled() {
        return marketplaceEnabled;
    }

    public boolean isFoodDeliveryEnabled() {
        return foodDeliveryEnabled;
    }

    public boolean isRideHailingEnabled() {
        return rideHailingEnabled;
    }

    public boolean isGroceryDeliveryEnabled() {
        return groceryDeliveryEnabled;
    }

    public boolean isVideoEnabled() {
        return videoEnabled;
    }

    public boolean isAdvertisingEnabled() {
        return advertisingEnabled;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public boolean isWalletEnabled() {
        return walletEnabled;
    }

    public boolean isAdminEnabled() {
        return adminEnabled;
    }

    public int getRateLimitRequests() {
        return rateLimitRequests;
    }

    public int getRateLimitWindow() {
        return rateLimitWindow;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public long getMaxImageSize() {
        return maxImageSize;
    }

    public long getMaxVideoSize() {
        return maxVideoSize;
    }

    public Map<String, Object> getFeatureFlags() {
        Map<String, Object> features = new HashMap<>();
        features.put("social", socialEnabled);
        features.put("messaging", messagingEnabled);
        features.put("marketplace", marketplaceEnabled);
        features.put("foodDelivery", foodDeliveryEnabled);
        features.put("rideHailing", rideHailingEnabled);
        features.put("groceryDelivery", groceryDeliveryEnabled);
        features.put("video", videoEnabled);
        features.put("advertising", advertisingEnabled);
        features.put("ai", aiEnabled);
        features.put("wallet", walletEnabled);
        features.put("admin", adminEnabled);
        return features;
    }

    public Map<String, Object> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", appName);
        info.put("version", appVersion);
        info.put("description", appDescription);
        info.put("features", getFeatureFlags());
        return info;
    }
}
