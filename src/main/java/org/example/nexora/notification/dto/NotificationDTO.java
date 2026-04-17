package org.example.nexora.notification.dto;

public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private String type;
    private String userId;
    private Boolean isRead;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}