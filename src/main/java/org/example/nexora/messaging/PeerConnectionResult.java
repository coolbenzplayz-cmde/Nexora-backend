package org.example.nexora.messaging;

import lombok.Data;
import java.util.List;

/**
 * Peer connection result
 */
@Data
public class PeerConnectionResult {
    
    private boolean success;
    private String connectionId;
    private String sdpOffer;
    private List<ICEServer> iceServers;
    private WebRTCPeerConnection peerConnection;
    private String errorMessage;
    
    public PeerConnectionResult() {
        this.success = false;
    }
    
    public static PeerConnectionResult success(String connectionId, String sdpOffer, List<ICEServer> iceServers, WebRTCPeerConnection peerConnection) {
        PeerConnectionResult result = new PeerConnectionResult();
        result.setSuccess(true);
        result.setConnectionId(connectionId);
        result.setSdpOffer(sdpOffer);
        result.setIceServers(iceServers);
        result.setPeerConnection(peerConnection);
        return result;
    }
    
    public static PeerConnectionResult failure(String errorMessage) {
        PeerConnectionResult result = new PeerConnectionResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
