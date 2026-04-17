package org.example.nexora.video;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 💬 ADD COMMENT
    @PostMapping
    public Comment addComment(@RequestHeader("Authorization") String token,
                              @RequestBody Comment comment) {
        return commentService.addComment(token, comment);
    }

    // 📺 GET COMMENTS
    @GetMapping("/{videoId}")
    public List<Comment> getComments(@PathVariable Long videoId) {
        return commentService.getComments(videoId);
    }

    // 🔁 GET REPLIES
    @GetMapping("/replies/{commentId}")
    public List<Comment> getReplies(@PathVariable Long commentId) {
        return commentService.getReplies(commentId);
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }
}