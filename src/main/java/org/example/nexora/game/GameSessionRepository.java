package org.example.nexora.game;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByGameCodeAndStatusOrderByScoreDesc(
            String gameCode,
            GameSessionStatus status,
            Pageable pageable);
}
