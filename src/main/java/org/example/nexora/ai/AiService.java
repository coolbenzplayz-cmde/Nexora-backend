package org.example.nexora.ai;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AiService {

    private final Map<String, List<String>> conversationHistory = new HashMap<>();
    
    private final Map<String, List<String>> smartReplies = new HashMap<>();
    
    public AiService() {
        initializeSmartReplies();
    }

    private void initializeSmartReplies() {
        smartReplies.put("greeting", Arrays.asList(
                "Hello! How can I help you today?",
                "Hi there! What can I assist you with?",
                "Hey! I'm here to help. What do you need?"
        ));
        
        smartReplies.put("help", Arrays.asList(
                "I can help you with: navigation, recommendations, account settings, ordering, and general questions.",
                "I'm here to assist! Ask me about our services, and I'll do my best to help.",
                "Need assistance? I can guide you through our app features."
        ));
        
        smartReplies.put("thanks", Arrays.asList(
                "You're welcome! Is there anything else I can help with?",
                "Happy to help! Let me know if you need anything else.",
                "My pleasure! Feel free to ask more questions."
        ));
        
        smartReplies.put("default", Arrays.asList(
                "I understand. Let me process that for you.",
                "Thanks for sharing. Here's what I can help with...",
                "I see. How about I provide some options for you?"
        ));
    }

    public String processMessage(String userId, String message) {
        String lowerMessage = message.toLowerCase();
        String response = generateResponse(lowerMessage);
        
        conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        conversationHistory.get(userId).add(message);
        conversationHistory.get(userId).add(response);
        
        if (conversationHistory.get(userId).size() > 100) {
            conversationHistory.get(userId).subList(0, 50).clear();
        }
        
        return response;
    }

    private String generateResponse(String message) {
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return getRandomReply("greeting");
        }
        
        if (message.contains("help") || message.contains("what can you do")) {
            return getRandomReply("help");
        }
        
        if (message.contains("thank") || message.contains("thanks")) {
            return getRandomReply("thanks");
        }
        
        if (message.contains("order") || message.contains("buy")) {
            return "I can help you place an order! Would you like to browse our marketplace, food delivery, or grocery items?";
        }
        
        if (message.contains("track") || message.contains("where")) {
            return "To track your order, go to the Orders section in your profile. You can see real-time updates there.";
        }
        
        if (message.contains("refund") || message.contains("return")) {
            return "I understand you need help with a refund or return. Please visit the Order Details page and select 'Request Return' or contact support.";
        }
        
        if (message.contains("payment") || message.contains("pay")) {
            return "We accept various payment methods including M-Pesa, credit cards, and digital wallets. You can manage payment methods in your wallet settings.";
        }
        
        if (message.contains("ride") || message.contains("taxi") || message.contains("uber")) {
            return "Need a ride? You can book a ride through our Ride-Hailing service. Just set your pickup location and destination!";
        }
        
        if (message.contains("food") || message.contains("eat") || message.contains("restaurant")) {
            return "Hungry? Check out our Food Delivery service for a variety of restaurants near you!";
        }
        
        if (message.contains("video") || message.contains("content") || message.contains("creator")) {
            return "Interested in creating content? Visit our Video platform to upload, stream, and monetize your content!";
        }
        
        if (message.contains("ad") || message.contains("advertise") || message.contains("promote")) {
            return "Want to promote your business? Check out our Advertising platform for campaign management tools.";
        }
        
        return getRandomReply("default");
    }

    private String getRandomReply(String category) {
        List<String> replies = smartReplies.get(category);
        if (replies == null || replies.isEmpty()) {
            return smartReplies.get("default").get(new Random().nextInt(smartReplies.get("default").size()));
        }
        return replies.get(new Random().nextInt(replies.size()));
    }

    public List<String> getSmartReplies(String context) {
        String lowerContext = context.toLowerCase();
        
        if (lowerContext.contains("hello") || lowerContext.contains("hi")) {
            return smartReplies.get("greeting");
        }
        
        if (lowerContext.contains("help")) {
            return smartReplies.get("help");
        }
        
        return smartReplies.get("default");
    }

    public List<String> getConversationHistory(String userId) {
        return conversationHistory.getOrDefault(userId, new ArrayList<>());
    }

    public List<String> getRecommendations(String userId, String category) {
        List<String> recommendations = new ArrayList<>();
        
        switch (category) {
            case "products":
                recommendations.add("Browse trending products in Marketplace");
                recommendations.add("Check out new arrivals in Electronics");
                recommendations.add("View featured Fashion items");
                break;
            case "food":
                recommendations.add("Popular restaurants near you");
                recommendations.add("Best rated cuisines");
                recommendations.add("Special offers and discounts");
                break;
            case "video":
                recommendations.add("Trending videos in your feed");
                recommendations.add("Creators you might like");
                recommendations.add("Live streams currently happening");
                break;
            case "ride":
                recommendations.add("Save your frequent locations");
                recommendations.add("Schedule a ride for later");
                recommendations.add("Check fare estimates");
                break;
            default:
                recommendations.add("Explore our Marketplace");
                recommendations.add("Order food from top restaurants");
                recommendations.add("Book your next ride");
        }
        
        return recommendations;
    }

    public Map<String, Object> analyzeContent(String content) {
        Map<String, Object> analysis = new HashMap<>();
        
        analysis.put("timestamp", LocalDateTime.now());
        analysis.put("contentLength", content.length());
        analysis.put("wordCount", content.split("\\s+").length);
        
        boolean containsSuspiciousPattern = checkSuspiciousPatterns(content);
        analysis.put("suspicious", containsSuspiciousPattern);
        
        double sentimentScore = calculateSentiment(content);
        analysis.put("sentiment", sentimentScore > 0.1 ? "positive" : sentimentScore < -0.1 ? "negative" : "neutral");
        
        List<String> keywords = extractKeywords(content);
        analysis.put("keywords", keywords);
        
        return analysis;
    }

    private boolean checkSuspiciousPatterns(String content) {
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("http://") || lowerContent.contains("https://")) {
            if (!lowerContent.contains("nexora.app")) {
                return true;
            }
        }
        
        String[] spamPatterns = {"click here", "free money", "win now", "congratulations", "act now"};
        for (String pattern : spamPatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    private double calculateSentiment(String content) {
        String lowerContent = content.toLowerCase();
        double score = 0;
        
        String[] positiveWords = {"good", "great", "excellent", "amazing", "love", "best", "awesome", "wonderful", "fantastic"};
        String[] negativeWords = {"bad", "terrible", "awful", "hate", "worst", "horrible", "poor", "disappointing"};
        
        for (String word : positiveWords) {
            if (lowerContent.contains(word)) score += 0.1;
        }
        
        for (String word : negativeWords) {
            if (lowerContent.contains(word)) score -= 0.1;
        }
        
        return Math.max(-1.0, Math.min(1.0, score));
    }

    private List<String> extractKeywords(String content) {
        String[] words = content.toLowerCase().split("\\s+");
        
        String[] commonWords = {"the", "is", "at", "which", "on", "a", "an", "and", "or", "but", "in", "with", "to", "for"};
        
        return Arrays.stream(words)
                .filter(word -> word.length() > 3)
                .filter(word -> Arrays.stream(commonWords).noneMatch(word::equals))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    public boolean checkFraudRisk(Map<String, Object> transactionData) {
        double amount = getDoubleValue(transactionData, "amount", 0.0);
        
        if (amount > 10000) {
            return true;
        }
        
        int frequency = getIntValue(transactionData, "frequency", 0);
        if (frequency > 5) {
            return true;
        }
        
        String location = getStringValue(transactionData, "location", "");
        if (!location.isEmpty() && isSuspiciousLocation(location)) {
            return true;
        }
        
        return false;
    }

    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private boolean isSuspiciousLocation(String location) {
        String[] suspiciousLocations = {"unknown", "offshore", "restricted"};
        for (String loc : suspiciousLocations) {
            if (location.toLowerCase().contains(loc)) {
                return true;
            }
        }
        return false;
    }
}
