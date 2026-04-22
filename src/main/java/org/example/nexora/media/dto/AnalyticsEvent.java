package org.example.nexora.media.dto;

import lombok.Data;
import org.example.nexora.media.entity.ContentAnalytics;
import java.util.Map;

@Data
public class AnalyticsEvent {
    private ContentAnalytics.AnalyticsEventType eventType;
    private String platform;
    private Map<String, Object> metrics;
    private String ipAddress;
    private String userAgent;
    private String referrer;
    private String geolocation;
    private String deviceType;
}
