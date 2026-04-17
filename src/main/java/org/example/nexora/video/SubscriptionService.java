package org.example.nexora.video;

import org.example.nexora.security.JwtService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               JwtService jwtService,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // 🔔 SUBSCRIBE
    public String subscribe(String token, Long creatorId) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getId().equals(creatorId)) {
            throw new RuntimeException("You cannot subscribe to yourself");
        }

        subscriptionRepository.findByFollowerIdAndCreatorId(user.getId(), creatorId)
                .ifPresent(sub -> {
                    throw new RuntimeException("Already subscribed");
                });

        subscriptionRepository.save(new Subscription(user.getId(), creatorId));

        return "Subscribed";
    }

    // ❌ UNSUBSCRIBE
    public String unsubscribe(String token, Long creatorId) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription sub = subscriptionRepository
                .findByFollowerIdAndCreatorId(user.getId(), creatorId)
                .orElseThrow(() -> new RuntimeException("Not subscribed"));

        subscriptionRepository.delete(sub);

        return "Unsubscribed";
    }

    // 👥 COUNT SUBSCRIBERS
    public int countSubscribers(Long creatorId) {
        return subscriptionRepository.findByCreatorId(creatorId).size();
    }

    // 📺 WHO I FOLLOW
    public List<Subscription> mySubscriptions(String token) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return subscriptionRepository.findByFollowerId(user.getId());
    }
}