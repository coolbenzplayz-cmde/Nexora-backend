package org.example.nexora.messaging;

import lombok.Data;

/**
 * ICE server configuration
 */
@Data
public class ICEServer {
    
    private String url;
    private String username;
    private String credential;
    private String credentialType;
    
    public ICEServer(String url) {
        this.url = url;
        this.credentialType = "password";
    }
    
    public ICEServer(String url, String username, String credential) {
        this.url = url;
        this.username = username;
        this.credential = credential;
        this.credentialType = "password";
    }
}
