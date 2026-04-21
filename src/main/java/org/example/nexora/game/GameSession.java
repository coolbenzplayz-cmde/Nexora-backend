package org.example.nexora.game;

import jakarta.persistence.*;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "game_sessions", indexes = {
        @Index(name = "idx_game_sessions_user", columnList = "user_id"),
        @Index(name = "idx_game_sessions_code_score", columnList = "game_code,score")
})
public class GameSession extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_code", nullable = false, length = 64)
    private String gameCode;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameSessionStatus status = GameSessionStatus.ACTIVE;

    // Default constructor
    public GameSession() {
    }

    // Full constructor
    public GameSession(Long userId, String gameCode, int score, GameSessionStatus status) {
        this.userId = userId;
        this.gameCode = gameCode;
        this.score = score;
        this.status = status;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GameSessionStatus getStatus() {
        return status;
    }

    public void setStatus(GameSessionStatus status) {
        this.status = status;
    }
}
