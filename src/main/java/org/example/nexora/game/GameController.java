package org.example.nexora.game;

import org.example.nexora.common.ApiResponse;
import org.example.nexora.game.dto.StartGameSessionRequest;
import org.example.nexora.game.dto.SubmitGameScoreRequest;
import org.example.nexora.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<GameProfile>> profile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(gameService.getOrCreateProfile(user)));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<GameSession>> startSession(
            Authentication authentication,
            @RequestBody StartGameSessionRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(gameService.startSession(user, request.getGameCode())));
    }

    @PostMapping("/sessions/{sessionId}/score")
    public ResponseEntity<ApiResponse<GameSession>> submitScore(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody SubmitGameScoreRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                gameService.submitScore(user, sessionId, request.getScore())));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<GameSession>>> leaderboard(@RequestParam String gameCode) {
        return ResponseEntity.ok(ApiResponse.success(gameService.leaderboard(gameCode)));
    }
}
