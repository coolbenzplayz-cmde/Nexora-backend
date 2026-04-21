package org.example.nexora.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.nexora.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "user_follows",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_follows_follower_following", columnNames = {"follower_id", "following_id"})
        },
        indexes = {
                @Index(name = "idx_user_follows_follower", columnList = "follower_id"),
                @Index(name = "idx_user_follows_following", columnList = "following_id")
        }
)
public class UserFollow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;
}
