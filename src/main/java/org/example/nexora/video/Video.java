package org.example.nexora.video;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title;

    private String videoUrl;

    private int views = 0;

    private int likes = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Video() {}

    public Video(Long userId, String title, String videoUrl) {
        this.userId = userId;
        this.title = title;
        this.videoUrl = videoUrl;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
    public int getViews() { return views; }
    public int getLikes() { return likes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setViews(int views) { this.views = views; }
    public void setLikes(int likes) { this.likes = likes; }
}