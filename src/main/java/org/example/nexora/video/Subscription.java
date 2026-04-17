package org.example.nexora.video;

import jakarta.persistence.*;

@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long followerId;   // who is following
    private Long creatorId;   // who is being followed

    public Subscription() {}

    public Subscription(Long followerId, Long creatorId) {
        this.followerId = followerId;
        this.creatorId = creatorId;
    }

    public Long getId() { return id; }
    public Long getFollowerId() { return followerId; }
    public Long getCreatorId() { return creatorId; }
}