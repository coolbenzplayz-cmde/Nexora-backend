package org.example.nexora.game.dto;

import lombok.Data;

@Data
public class StartGameSessionRequest {
    private String gameCode;

    // Explicit getters to ensure they exist
    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }
}
