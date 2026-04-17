package org.example.nexora.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameProfileRepository extends JpaRepository<GameProfile, Long> {

    Optional<GameProfile> findByUserId(Long userId);
}
