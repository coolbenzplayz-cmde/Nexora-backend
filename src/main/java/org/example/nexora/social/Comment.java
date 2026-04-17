package org.example.nexora.social;

import jakarta.persistence.*;
import org.example.nexora.common.BaseEntity;

import java.time.LocalDateTime;

@Entity(name = "SocialComment")
@Table(name = "comments")
public class Comment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private org.example.nexora.user.User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    private int repliesCount = 0;

    public Comment() {}

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public int getRepliesCount() {
        return repliesCount;
    }

    public void setRepliesCount(int repliesCount) {
        this.repliesCount = repliesCount;
    }
}