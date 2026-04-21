package org.example.nexora.game;

import jakarta.persistence.*;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "game_profiles", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class GameProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private long totalXp;

    @Column(nullable = false)
    private int level;

    // Default constructor
    public GameProfile() {
    }

    // Full constructor
    public GameProfile(Long userId, long totalXp, int level) {
        this.userId = userId;
        this.totalXp = totalXp;
        this.level = level;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(long totalXp) {
        this.totalXp = totalXp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
