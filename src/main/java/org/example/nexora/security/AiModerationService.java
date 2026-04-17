package org.example.nexora.security;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AiModerationService {

    // 🚫 banned words list (expand later)
    private final List<String> bannedWords = Arrays.asList(
            "nude", "sex", "kill", "drugs", "porn"
    );

    // 🧠 analyze text
    public String checkContent(String text) {

        if (text == null) {
            return "BLOCK";
        }

        String lower = text.toLowerCase();

        // 🚫 check bad words
        for (String word : bannedWords) {
            if (lower.contains(word)) {
                return "BLOCK";
            }
        }

        // ⚠️ simple spam detection
        if (text.length() > 5000) {
            return "FLAG";
        }

        // ✅ safe content
        return "APPROVE";
    }
}