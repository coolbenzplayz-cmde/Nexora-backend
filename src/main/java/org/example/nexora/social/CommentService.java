package org.example.nexora.social;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.video.Video;
import org.example.nexora.video.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public Comment createCommentOnVideo(Long userId, Long videoId, String content, Long parentCommentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException("Video not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setVideoId(videoId);
        comment.setContentType(Comment.ContentType.VIDEO);
        comment.setContent(content.trim());
        comment.setParentCommentId(parentCommentId);

        Comment savedComment = commentRepository.save(comment);

        // Update parent comment replies count if this is a reply
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new BusinessException("Parent comment not found"));
            parentComment.incrementRepliesCount();
            commentRepository.save(parentComment);
        }

        // Update video comment count
        updateVideoCommentCount(videoId);

        log.info("Comment created on video {} by user {}", videoId, userId);
        return savedComment;
    }

    @Transactional
    public Comment createCommentOnPost(Long userId, Long postId, String content, Long parentCommentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContentType(Comment.ContentType.POST);
        comment.setContent(content.trim());
        comment.setParentCommentId(parentCommentId);

        Comment savedComment = commentRepository.save(comment);

        // Update parent comment replies count if this is a reply
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new BusinessException("Parent comment not found"));
            parentComment.incrementRepliesCount();
            commentRepository.save(parentComment);
        }

        log.info("Comment created on post {} by user {}", postId, userId);
        return savedComment;
    }

    @Transactional
    public Comment editComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("You can only edit your own comments");
        }

        if (comment.getIsDeleted()) {
            throw new BusinessException("Cannot edit deleted comments");
        }

        comment.editContent(newContent.trim());
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("You can only delete your own comments");
        }

        if (comment.getIsDeleted()) {
            throw new BusinessException("Comment already deleted");
        }

        comment.softDelete();
        commentRepository.save(comment);

        // Update parent comment replies count if this was a reply
        if (comment.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(comment.getParentCommentId())
                    .orElse(null);
            if (parentComment != null) {
                parentComment.decrementRepliesCount();
                commentRepository.save(parentComment);
            }
        }

        // Update video comment count if this was a video comment
        if (comment.getVideoId() != null) {
            updateVideoCommentCount(comment.getVideoId());
        }
    }

    public Page<Comment> getTopLevelCommentsByVideo(Long videoId, Pageable pageable) {
        return commentRepository.findTopLevelCommentsByVideoId(videoId, pageable);
    }

    public Page<Comment> getTopLevelCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findTopLevelCommentsByPostId(postId, pageable);
    }

    public Page<Comment> getRepliesByParentComment(Long parentCommentId, Pageable pageable) {
        return commentRepository.findRepliesByParentCommentId(parentCommentId, pageable);
    }

    public Page<Comment> getAllCommentsByVideo(Long videoId, Pageable pageable) {
        return commentRepository.findAllCommentsByVideoId(videoId, pageable);
    }

    public Page<Comment> getAllCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findAllCommentsByPostId(postId, pageable);
    }

    public Page<Comment> getCommentsByUser(Long userId, Pageable pageable) {
        return commentRepository.findCommentsByUserId(userId, pageable);
    }

    public long getCommentCountByVideo(Long videoId) {
        return commentRepository.countCommentsByVideoId(videoId);
    }

    public long getCommentCountByPost(Long postId) {
        return commentRepository.countCommentsByPostId(postId);
    }

    public long getCommentCountByUser(Long userId) {
        return commentRepository.countCommentsByUserId(userId);
    }

    public long getReplyCountByParentComment(Long parentCommentId) {
        return commentRepository.countRepliesByParentCommentId(parentCommentId);
    }

    public Page<Comment> getFlaggedComments(Pageable pageable) {
        return commentRepository.findFlaggedComments(pageable);
    }

    public Page<Comment> searchComments(String keyword, Pageable pageable) {
        return commentRepository.searchCommentsByKeyword(keyword, pageable);
    }

    @Transactional
    public void flagComment(Long commentId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        comment.flagContent(reason);
        commentRepository.save(comment);
        log.info("Comment {} flagged for moderation: {}", commentId, reason);
    }

    @Transactional
    public void unflagComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        comment.unflagContent();
        commentRepository.save(comment);
        log.info("Comment {} unflagged", commentId);
    }

    @Transactional
    public void incrementCommentLikes(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        comment.incrementLikesCount();
        commentRepository.save(comment);
    }

    @Transactional
    public void decrementCommentLikes(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        comment.decrementLikesCount();
        commentRepository.save(comment);
    }

    @Transactional
    private void updateVideoCommentCount(Long videoId) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video != null) {
            long commentCount = commentRepository.countCommentsByVideoId(videoId);
            video.setComments(commentCount);
            videoRepository.save(video);
        }
    }

    public List<Comment> getRepliesByParentCommentIds(List<Long> parentCommentIds) {
        return commentRepository.findRepliesByParentCommentIds(parentCommentIds);
    }

    public Page<Comment> getPopularCommentsByVideo(Long videoId, Pageable pageable) {
        return commentRepository.findPopularCommentsByVideoId(videoId, pageable);
    }

    public Page<Comment> getPopularCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findPopularCommentsByPostId(postId, pageable);
    }
}
