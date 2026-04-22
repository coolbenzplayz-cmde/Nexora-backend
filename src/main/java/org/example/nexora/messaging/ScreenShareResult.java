package org.example.nexora.messaging;

import lombok.Data;

/**
 * Screen share result
 */
@Data
public class ScreenShareResult {
    
    private boolean success;
    private String streamId;
    private String resolution;
    private int frameRate;
    private String errorMessage;
    private String shareUrl;
    
    public ScreenShareResult() {
        this.success = false;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }
    
    public static ScreenShareResult success(String streamId, String resolution, int frameRate) {
        ScreenShareResult result = new ScreenShareResult();
        result.setSuccess(true);
        result.setStreamId(streamId);
        result.setResolution(resolution);
        result.setFrameRate(frameRate);
        return result;
    }
    
    public static ScreenShareResult failure(String errorMessage) {
        ScreenShareResult result = new ScreenShareResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
