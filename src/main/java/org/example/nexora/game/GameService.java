package org.example.nexora.game;

import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {

    private static final int XP_PER_POINT = 10;
    private static final int LEADERBOARD_SIZE = 20;

    private final GameProfileRepository gameProfileRepository;
    private final GameSessionRepository gameSessionRepository;

    public GameService(GameProfileRepository gameProfileRepository,
                       GameSessionRepository gameSessionRepository) {
        this.gameProfileRepository = gameProfileRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    @Transactional(readOnly = true)
    public GameProfile getOrCreateProfile(User user) {
        return gameProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    GameProfile created = new GameProfile();
                    created.setUserId(user.getId());
                    created.setTotalXp(0);
                    created.setLevel(1);
                    return gameProfileRepository.save(created);
                });
    }

    @Transactional
    public GameSession startSession(User user, String gameCode) {
        if (gameCode == null || gameCode.isBlank()) {
            throw new BusinessException("gameCode is required", "GAME_CODE_REQUIRED");
        }
        getOrCreateProfile(user);
        GameSession session = new GameSession();
        session.setUserId(user.getId());
        session.setGameCode(gameCode.trim());
        session.setScore(0);
        session.setStatus(GameSession.GameSessionStatus.ACTIVE);
        return gameSessionRepository.save(session);
    }

    @Transactional
    public GameSession submitScore(User user, Long sessionId, int score) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Session not found", "SESSION_NOT_FOUND"));
        if (!session.getUserId().equals(user.getId())) {
            throw new BusinessException("Forbidden", "FORBIDDEN");
        }
        if (session.getStatus() != GameSession.GameSessionStatus.ACTIVE) {
            throw new BusinessException("Session already completed", "SESSION_CLOSED");
        }
        session.setScore(Math.max(0, score));
        session.setStatus(GameSession.GameSessionStatus.COMPLETED);
        GameSession saved = gameSessionRepository.save(session);

        GameProfile profile = getOrCreateProfile(user);
        long xpGain = (long) saved.getScore() * XP_PER_POINT;
        profile.setTotalXp(profile.getTotalXp() + xpGain);
        profile.setLevel(1 + (int) (profile.getTotalXp() / 1000));
        gameProfileRepository.save(profile);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<GameSession> leaderboard(String gameCode) {
        if (gameCode == null || gameCode.isBlank()) {
            throw new BusinessException("gameCode is required", "GAME_CODE_REQUIRED");
        }
        return gameSessionRepository.findByGameCodeAndStatusOrderByScoreDesc(
                gameCode.trim(),
                GameSession.GameSessionStatus.COMPLETED,
                PageRequest.of(0, LEADERBOARD_SIZE));
    }
}
