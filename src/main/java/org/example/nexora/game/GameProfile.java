package org.example.nexora.game;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "game_profiles", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GameProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private long totalXp;

    @Column(nullable = false)
    private int level;
}
