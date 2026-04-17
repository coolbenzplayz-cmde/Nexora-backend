package org.example.nexora.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByFollowerIdAndCreatorId(Long followerId, Long creatorId);

    List<Subscription> findByCreatorId(Long creatorId);

    List<Subscription> findByFollowerId(Long followerId);
}