package org.example.nexora.messaging;

import lombok.Data;

/**
 * WebRTC peer connection
 */
@Data
public class WebRTCPeerConnection {
    
    private String connectionId;
    private String localDescription;
    private String remoteDescription;
    private String state;
    private boolean connected;
    
    public WebRTCPeerConnection(String connectionId) {
        this.connectionId = connectionId;
        this.state = "NEW";
        this.connected = false;
    }
    
    public boolean isConnected() {
        return connected && "CONNECTED".equals(state);
    }
}
