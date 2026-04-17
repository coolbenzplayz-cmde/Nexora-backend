package org.example.nexora.video;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Earning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long videoId;

    private BigDecimal amount;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Earning() {}

    public Earning(Long userId, Long videoId, BigDecimal amount) {
        this.userId = userId;
        this.videoId = videoId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getVideoId() { return videoId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}