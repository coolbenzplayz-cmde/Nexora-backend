package org.example.nexora.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> request) {
        
        String message = request.get("message");
        String response = aiService.processMessage(userId, message);
        
        return ResponseEntity.ok(Map.of(
                "response", response,
                "userId", userId
        ));
    }

    @GetMapping("/smart-replies")
    public ResponseEntity<List<String>> getSmartReplies(@RequestParam String context) {
        return ResponseEntity.ok(aiService.getSmartReplies(context));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<String>> getRecommendations(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "default") String category) {
        return ResponseEntity.ok(aiService.getRecommendations(userId, category));
    }

    @PostMapping("/analyze-content")
    public ResponseEntity<Map<String, Object>> analyzeContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        return ResponseEntity.ok(aiService.analyzeContent(content));
    }

    @PostMapping("/fraud-check")
    public ResponseEntity<Map<String, Object>> checkFraudRisk(@RequestBody Map<String, Object> transactionData) {
        boolean isHighRisk = aiService.checkFraudRisk(transactionData);
        
        return ResponseEntity.ok(Map.of(
                "isHighRisk", isHighRisk,
                "riskLevel", isHighRisk ? "HIGH" : "LOW",
                "message", isHighRisk ? "Transaction flagged for review" : "Transaction approved"
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<List<String>> getConversationHistory(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(aiService.getConversationHistory(userId));
    }
}
