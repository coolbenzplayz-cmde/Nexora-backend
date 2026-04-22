package org.example.nexora.media.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleRequest {
    private LocalDateTime scheduledAt;
    private List<String> publishingPlatforms;
    private String caption;
    private String[] hashtags;
    private Boolean notifyFollowers = true;
    private String timezone;
}
