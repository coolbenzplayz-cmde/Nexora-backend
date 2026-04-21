package org.example.nexora.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    private Long userId;
    private Set<String> preferredCategories = new HashSet<>();
    private Set<Long> preferredCreators = new HashSet<>();
    private Set<String> blockedCategories = new HashSet<>();
    private Set<Long> blockedCreators = new HashSet<>();
    private Set<String> preferredTags = new HashSet<>();
    private Integer preferredMinDuration = 10;
    private Integer preferredMaxDuration = 300;
    private Double preferredContentQuality = 0.5; // 0.0 = low quality, 1.0 = high quality
    private Boolean prefersVerifiedCreators = false;
    private Boolean prefersNewContent = true;
    private Boolean prefersPopularContent = false;
    private Double diversityPreference = 0.7; // 0.0 = similar content, 1.0 = diverse content
    private Double languagePreference = 1.0; // How much to prefer same language content
    private Double locationPreference = 0.3; // How much to prefer local content
    private String preferredLanguage = "en";
    private String timeZone = "UTC";
    private Boolean enableAdultContent = false;
    private Boolean enableViolentContent = false;
    private Double sensitivityLevel = 0.5; // Content sensitivity filtering

    public void addPreferredCategory(String category) {
        preferredCategories.add(category);
    }

    public void addPreferredCreator(Long creatorId) {
        preferredCreators.add(creatorId);
    }

    public void addBlockedCategory(String category) {
        blockedCategories.add(category);
    }

    public void addBlockedCreator(Long creatorId) {
        blockedCreators.add(creatorId);
    }

    public boolean isCategoryPreferred(String category) {
        return preferredCategories.contains(category);
    }

    public boolean isCategoryBlocked(String category) {
        return blockedCategories.contains(category);
    }

    public boolean isCreatorPreferred(Long creatorId) {
        return preferredCreators.contains(creatorId);
    }

    public boolean isCreatorBlocked(Long creatorId) {
        return blockedCreators.contains(creatorId);
    }

    public boolean isDurationPreferred(Integer duration) {
        if (duration == null) return true;
        return (preferredMinDuration == null || duration >= preferredMinDuration) &&
               (preferredMaxDuration == null || duration <= preferredMaxDuration);
    }
}
