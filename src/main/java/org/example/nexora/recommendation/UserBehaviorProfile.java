package org.example.nexora.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorProfile {

    private Long userId;
    private Map<Long, Double> interactionHistory = new HashMap<>(); // videoId -> interactionScore
    private Map<String, Integer> categoryPreferences = new HashMap<>(); // category -> preferenceCount
    private Map<Long, Integer> creatorPreferences = new HashMap<>(); // creatorId -> interactionCount
    private Set<Long> watchedVideoIds;
    private Set<Long> likedVideoIds;
    private Set<Long> sharedVideoIds;
    private Set<Long> commentedVideoIds;
    private Map<Integer, Double> timeOfDayPreferences = new HashMap<>(); // hour -> preferenceScore
    private Map<Integer, Double> dayOfWeekPreferences = new HashMap<>(); // dayOfWeek -> preferenceScore
    private double averageEngagementScore = 0.0;
    private double averageWatchTime = 0.0;
    private boolean prefersFreshContent = true;
    private boolean prefersShortContent = false;
    private boolean prefersHighQuality = false;
    private LocalDateTime lastInteraction;
    private int totalInteractions = 0;
    private double sessionDuration = 0.0;
    private double interactionFrequency = 0.0;

    public boolean hasInteractedWithVideo(Long videoId) {
        return interactionHistory.containsKey(videoId);
    }

    public double getVideoInteractionScore(Long videoId) {
        return interactionHistory.getOrDefault(videoId, 0.0);
    }

    public void addVideoInteraction(Long videoId, double score) {
        interactionHistory.put(videoId, score);
        totalInteractions++;
        lastInteraction = LocalDateTime.now();
        updateEngagementScore();
    }

    public void addCategoryPreference(String category) {
        categoryPreferences.merge(category, 1, Integer::sum);
    }

    public void addCreatorPreference(Long creatorId) {
        creatorPreferences.merge(creatorId, 1, Integer::sum);
    }

    public void updateTimeOfDayPreference(int hour) {
        timeOfDayPreferences.merge(hour, 1.0, Double::sum);
    }

    public void updateDayOfWeekPreference(int dayOfWeek) {
        dayOfWeekPreferences.merge(dayOfWeek, 1.0, Double::sum);
    }

    private void updateEngagementScore() {
        if (interactionHistory.isEmpty()) {
            averageEngagementScore = 0.0;
            return;
        }
        
        double totalScore = interactionHistory.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        
        averageEngagementScore = totalScore / interactionHistory.size();
    }

    public boolean hasEnoughDataForRecommendations() {
        return totalInteractions >= 5;
    }

    public double getCategoryPreferenceScore(String category) {
        int count = categoryPreferences.getOrDefault(category, 0);
        return totalInteractions > 0 ? (double) count / totalInteractions : 0.0;
    }

    public double getCreatorPreferenceScore(Long creatorId) {
        int count = creatorPreferences.getOrDefault(creatorId, 0);
        return totalInteractions > 0 ? (double) count / totalInteractions : 0.0;
    }
}
