package org.example.nexora.social;

import jakarta.persistence.*;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "likes")
public class Like extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private org.example.nexora.user.User user;

    public Like() {}

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public org.example.nexora.user.User getUser() {
        return user;
    }

    public void setUser(org.example.nexora.user.User user) {
        this.user = user;
    }
}