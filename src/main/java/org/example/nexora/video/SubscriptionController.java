package org.example.nexora.video;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // SUBSCRIBE
    @PostMapping("/subscribe/{creatorId}")
    public String subscribe(@RequestHeader("Authorization") String token,
                            @PathVariable Long creatorId) {
        return subscriptionService.subscribe(token, creatorId);
    }

    // UNSUBSCRIBE
    @PostMapping("/unsubscribe/{creatorId}")
    public String unsubscribe(@RequestHeader("Authorization") String token,
                              @PathVariable Long creatorId) {
        return subscriptionService.unsubscribe(token, creatorId);
    }

    // COUNT
    @GetMapping("/count/{creatorId}")
    public int count(@PathVariable Long creatorId) {
        return subscriptionService.countSubscribers(creatorId);
    }

    // MY SUBSCRIPTIONS
    @GetMapping("/me")
    public List<Subscription> mySubs(@RequestHeader("Authorization") String token) {
        return subscriptionService.mySubscriptions(token);
    }
}