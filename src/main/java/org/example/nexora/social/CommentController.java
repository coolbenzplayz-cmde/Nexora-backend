package org.example.nexora.social;

import lombok.RequiredArgsConstructor;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.example.nexora.social.dto.CreateCommentRequest;
import org.example.nexora.social.dto.EditCommentRequest;
import org.example.nexora.social.dto.FlagCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/video/{videoId}")
    public ResponseEntity<ApiResponse<Comment>> createCommentOnVideo(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long videoId,
            @Valid @RequestBody CreateCommentRequest request) {
        
        Comment comment = commentService.createCommentOnVideo(
                currentUser.getId(), 
                videoId, 
                request.getContent(), 
                request.getParentCommentId()
        );
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment created successfully"));
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<Comment>> createCommentOnPost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        
        Comment comment = commentService.createCommentOnPost(
                currentUser.getId(), 
                postId, 
                request.getContent(), 
                request.getParentCommentId()
        );
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment created successfully"));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Comment>> editComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long commentId,
            @Valid @RequestBody EditCommentRequest request) {
        
        Comment comment = commentService.editComment(commentId, currentUser.getId(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment updated successfully"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long commentId) {
        
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }

    @GetMapping("/video/{videoId}/top-level")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getTopLevelCommentsByVideo(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentService.getTopLevelCommentsByVideo(videoId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/post/{postId}/top-level")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getTopLevelCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentService.getTopLevelCommentsByPost(postId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/replies/{parentCommentId}")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getRepliesByParentComment(
            @PathVariable Long parentCommentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Comment> replies = commentService.getRepliesByParentComment(parentCommentId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(replies.getContent())
                .page(replies.getNumber())
                .size(replies.getSize())
                .totalElements(replies.getTotalElements())
                .totalPages(replies.getTotalPages())
                .first(replies.isFirst())
                .last(replies.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/video/{videoId}/all")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getAllCommentsByVideo(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentService.getAllCommentsByVideo(videoId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/post/{postId}/all")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getAllCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentService.getAllCommentsByPost(postId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentService.getCommentsByUser(userId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/video/{videoId}/count")
    public ResponseEntity<ApiResponse<Long>> getCommentCountByVideo(@PathVariable Long videoId) {
        long count = commentService.getCommentCountByVideo(videoId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<ApiResponse<Long>> getCommentCountByPost(@PathVariable Long postId) {
        long count = commentService.getCommentCountByPost(postId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> getCommentCountByUser(@PathVariable Long userId) {
        long count = commentService.getCommentCountByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/replies/{parentCommentId}/count")
    public ResponseEntity<ApiResponse<Long>> getReplyCountByParentComment(@PathVariable Long parentCommentId) {
        long count = commentService.getReplyCountByParentComment(parentCommentId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getFlaggedComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentService.getFlaggedComments(pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentService.searchComments(keyword, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{commentId}/flag")
    public ResponseEntity<ApiResponse<Void>> flagComment(
            @PathVariable Long commentId,
            @RequestBody FlagCommentRequest request) {
        
        commentService.flagComment(commentId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(null, "Comment flagged successfully"));
    }

    @PostMapping("/{commentId}/unflag")
    public ResponseEntity<ApiResponse<Void>> unflagComment(@PathVariable Long commentId) {
        commentService.unflagComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment unflagged successfully"));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> incrementCommentLikes(@PathVariable Long commentId) {
        commentService.incrementCommentLikes(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment liked successfully"));
    }

    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> decrementCommentLikes(@PathVariable Long commentId) {
        commentService.decrementCommentLikes(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment unliked successfully"));
    }

    @GetMapping("/video/{videoId}/popular")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getPopularCommentsByVideo(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getPopularCommentsByVideo(videoId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/post/{postId}/popular")
    public ResponseEntity<ApiResponse<PaginationResponse<Comment>>> getPopularCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getPopularCommentsByPost(postId, pageable);
        
        PaginationResponse<Comment> response = PaginationResponse.<Comment>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
