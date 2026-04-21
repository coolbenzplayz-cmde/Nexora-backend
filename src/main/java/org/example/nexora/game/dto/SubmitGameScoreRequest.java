package org.example.nexora.game.dto;

import lombok.Data;

@Data
public class SubmitGameScoreRequest {
    private int score;

    // Explicit getters to ensure they exist
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
