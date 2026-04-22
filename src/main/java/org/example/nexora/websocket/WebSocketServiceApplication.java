package org.example.nexora.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WebSocket Service - Handles real-time communications
 * 
 * Features:
 * - WebSocket connections
 * - Real-time messaging
 * - Live notifications
 * - Presence management
 * - Chat rooms
 * - Video calling signaling
 * - Live streaming
 * - Event broadcasting
 */
@SpringBootApplication
@EnableFeignClients
public class WebSocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketServiceApplication.class, args);
    }
}
