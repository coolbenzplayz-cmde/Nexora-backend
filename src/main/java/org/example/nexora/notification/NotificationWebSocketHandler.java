package org.example.nexora.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket handler for real-time notifications
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // Store user sessions
    private final Map<Long, CopyOnWriteArrayList<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    // Store session to user mapping
    private final Map<WebSocketSession, Long> sessionUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract user ID from session attributes or query parameters
        Long userId = extractUserId(session);
        
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
            sessionUsers.put(session, userId);
            
            log.info("User {} connected with session {}", userId, session.getId());
            
            // Send connection confirmation
            sendConnectionConfirmation(session, userId);
        } else {
            log.warn("Connection established without user ID, closing session");
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = sessionUsers.remove(session);
        
        if (userId != null) {
            CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            
            log.info("User {} disconnected with session {}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = sessionUsers.get(session);
        
        if (userId != null) {
            String payload = message.getPayload();
            log.debug("Received message from user {}: {}", userId, payload);
            
            // Handle different message types
            handleMessage(session, userId, payload);
        }
    }

    /**
     * Send notification to a specific user
     */
    public void sendNotification(Long userId, Notification notification) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        
        if (sessions != null && !sessions.isEmpty()) {
            String notificationJson = convertNotificationToJson(notification);
            
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(notificationJson));
                        log.debug("Sent notification {} to user {} via session {}", 
                                notification.getId(), userId, session.getId());
                    }
                } catch (IOException e) {
                    log.error("Failed to send notification to session {}: {}", session.getId(), e.getMessage());
                    // Remove failed session
                    sessions.remove(session);
                    sessionUsers.remove(session);
                }
            }
        }
    }

    /**
     * Check if user is connected
     */
    public boolean isUserConnected(Long userId) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty() && 
               sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    /**
     * Get connected user count
     */
    public int getConnectedUserCount() {
        return userSessions.size();
    }

    /**
     * Get total session count
     */
    public int getTotalSessionCount() {
        return userSessions.values().stream()
                .mapToInt(CopyOnWriteArrayList::size)
                .sum();
    }

    /**
     * Send system message to all connected users
     */
    public void broadcastToAll(String message) {
        String systemMessage = createSystemMessage(message);
        
        userSessions.values().forEach(sessions -> {
            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(systemMessage));
                    }
                } catch (IOException e) {
                    log.error("Failed to broadcast to session {}: {}", session.getId(), e.getMessage());
                }
            });
        });
    }

    /**
     * Send message to specific user
     */
    public void sendToUser(Long userId, String message) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        
        if (sessions != null) {
            String userMessage = createUserMessage(message);
            
            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(userMessage));
                    }
                } catch (IOException e) {
                    log.error("Failed to send message to user {} session {}: {}", 
                            userId, session.getId(), e.getMessage());
                }
            });
        }
    }

    /**
     * Extract user ID from WebSocket session
     */
    private Long extractUserId(WebSocketSession session) {
        // Try to get from session attributes first
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        if (userIdAttr instanceof String) {
            try {
                return Long.parseLong((String) userIdAttr);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in session attributes: {}", userIdAttr);
            }
        }
        
        // Try to get from query parameters
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    try {
                        return Long.parseLong(param.substring(7));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid user ID in query: {}", param);
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Send connection confirmation
     */
    private void sendConnectionConfirmation(WebSocketSession session, Long userId) {
        try {
            String confirmation = String.format(
                "{\"type\":\"connection\",\"status\":\"connected\",\"userId\":%d,\"timestamp\":%d}",
                userId, System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(confirmation));
        } catch (IOException e) {
            log.error("Failed to send connection confirmation: {}", e.getMessage());
        }
    }

    /**
     * Handle incoming messages from clients
     */
    private void handleMessage(WebSocketSession session, Long userId, String payload) {
        try {
            // Parse JSON message (simplified)
            if (payload.contains("\"type\":\"ping\"")) {
                // Respond to ping
                String pong = "{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}";
                session.sendMessage(new TextMessage(pong));
            } else if (payload.contains("\"type\":\"mark_read\"")) {
                // Handle mark as read
                handleMarkAsRead(session, userId, payload);
            } else if (payload.contains("\"type\":\"get_unread_count\"")) {
                // Handle unread count request
                handleGetUnreadCount(session, userId);
            }
        } catch (IOException e) {
            log.error("Failed to handle message from user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Handle mark as read request
     */
    private void handleMarkAsRead(WebSocketSession session, Long userId, String payload) throws IOException {
        // Extract notification ID from payload (simplified)
        // In a real implementation, would parse JSON properly
        String response = "{\"type\":\"mark_read_result\",\"status\":\"success\"}";
        session.sendMessage(new TextMessage(response));
    }

    /**
     * Handle unread count request
     */
    private void handleGetUnreadCount(WebSocketSession session, Long userId) throws IOException {
        // Get actual unread count from notification service
        // For now, return a placeholder
        String response = String.format(
            "{\"type\":\"unread_count\",\"count\":%d,\"timestamp\":%d}",
            5, System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(response));
    }

    /**
     * Convert notification to JSON
     */
    private String convertNotificationToJson(Notification notification) {
        // Simplified JSON conversion
        return String.format(
            "{\"type\":\"notification\",\"id\":%d,\"title\":\"%s\",\"message\":\"%s\",\"type\":\"%s\",\"referenceType\":\"%s\",\"referenceId\":%s,\"isRead\":%b,\"createdAt\":\"%s\",\"priority\":\"%s\"}",
            notification.getId(),
            escapeJson(notification.getTitle()),
            escapeJson(notification.getMessage()),
            notification.getType(),
            notification.getReferenceType(),
            notification.getReferenceId(),
            notification.getIsRead(),
            notification.getCreatedAt().toString(),
            notification.getPriority()
        );
    }

    /**
     * Create system message
     */
    private String createSystemMessage(String message) {
        return String.format(
            "{\"type\":\"system\",\"message\":\"%s\",\"timestamp\":%d}",
            escapeJson(message), System.currentTimeMillis()
        );
    }

    /**
     * Create user message
     */
    private String createUserMessage(String message) {
        return String.format(
            "{\"type\":\"message\",\"message\":\"%s\",\"timestamp\":%d}",
            escapeJson(message), System.currentTimeMillis()
        );
    }

    /**
     * Escape JSON strings
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Clean up inactive sessions
     */
    public void cleanupInactiveSessions() {
        userSessions.values().forEach(sessions -> {
            sessions.removeIf(session -> !session.isOpen());
        });
        
        userSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        sessionUsers.entrySet().removeIf(entry -> !entry.getKey().isOpen());
        
        log.info("Cleaned up inactive sessions. Active users: {}, Total sessions: {}", 
                userSessions.size(), getTotalSessionCount());
    }
}
