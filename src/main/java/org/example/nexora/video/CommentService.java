package org.example.nexora.video;

import org.example.nexora.security.AiModerationService;
import org.example.nexora.security.JwtService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AiModerationService aiModerationService;

    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          JwtService jwtService,
                          AiModerationService aiModerationService) {

        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.aiModerationService = aiModerationService;
    }

    // 💬 ADD COMMENT
    public Comment addComment(String token, Comment comment) {

        String email = jwtService.extractEmail(token.replace("Bearer ", ""));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🤖 AI MODERATION CHECK
        String result = aiModerationService.checkContent(comment.getContent());

        if (result.equals("BLOCK")) {
            throw new RuntimeException("Comment blocked by AI moderation");
        }

        Comment newComment = new Comment(
                comment.getVideoId(),
                user.getId(),
                comment.getContent(),
                comment.getParentId()
        );

        return commentRepository.save(newComment);
    }

    // 📺 GET COMMENTS FOR VIDEO
    public List<Comment> getComments(Long videoId) {
        return commentRepository.findByVideoId(videoId);
    }

    // 🔁 GET REPLIES
    public List<Comment> getReplies(Long commentId) {
        return commentRepository.findByParentId(commentId);
    }

    // ❌ DELETE COMMENT
    public String deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
        return "Comment deleted";
    }
}