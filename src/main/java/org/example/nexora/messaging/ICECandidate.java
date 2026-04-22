package org.example.nexora.messaging;

import lombok.Data;

/**
 * ICE Candidate for WebRTC - standalone class for proper access
 */
@Data
public class ICECandidate {
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
    private String usernameFragment;
    
    public ICECandidate() {}
    
    public ICECandidate(String candidate, String sdpMid, int sdpMLineIndex) {
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
    }
}
