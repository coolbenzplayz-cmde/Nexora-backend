package org.example.nexora.social;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "follows", indexes = {
        @Index(name = "idx_follows_follower_id", columnList = "followerId"),
        @Index(name = "idx_follows_following_id", columnList = "followingId"),
        @Index(name = "idx_follows_created_at", columnList = "createdAt"),
        @Index(name = "idx_follows_unique", columnList = "followerId, followingId", unique = true)
})
public class Follow extends BaseEntity {

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "unfollowed_at")
    private LocalDateTime unfollowedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void unfollow() {
        this.isActive = false;
        this.unfollowedAt = LocalDateTime.now();
    }

    public void refollow() {
        this.isActive = true;
        this.unfollowedAt = null;
    }
}
