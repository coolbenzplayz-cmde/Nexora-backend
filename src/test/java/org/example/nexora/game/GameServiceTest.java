package org.example.nexora.game;

import org.example.nexora.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameProfileRepository gameProfileRepository;
    @Mock
    private GameSessionRepository gameSessionRepository;
    @InjectMocks
    private GameService gameService;

    @Test
    void startSessionCreatesRow() {
        User user = new User();
        user.setId(5L);
        when(gameProfileRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(gameProfileRepository.save(any(GameProfile.class))).thenAnswer(invocation -> {
            GameProfile p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> {
            GameSession s = invocation.getArgument(0);
            s.setId(99L);
            return s;
        });

        GameSession session = gameService.startSession(user, "arcade-runner");

        assertNotNull(session);
        assertEquals(99L, session.getId());
        assertEquals("arcade-runner", session.getGameCode());
        verify(gameSessionRepository).save(any(GameSession.class));
    }
}
