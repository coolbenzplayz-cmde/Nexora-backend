package org.example.nexora.game;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "game_sessions", indexes = {
        @Index(name = "idx_game_sessions_user", columnList = "user_id"),
        @Index(name = "idx_game_sessions_code_score", columnList = "game_code,score")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
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

    public enum GameSessionStatus {
        ACTIVE,
        COMPLETED
    }
}
