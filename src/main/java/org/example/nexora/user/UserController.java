package org.example.nexora.user;

import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<User> users = userService.getAllUsers();
        PaginationResponse<User> response = new PaginationResponse<>(
            users, page, size, users.size()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String id,
            @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable String id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User activated"));
    }

    @PostMapping("/{currentUserId}/follow/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable String currentUserId,
            @PathVariable String targetUserId) {
        userService.followUser(currentUserId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Followed successfully"));
    }

    @PostMapping("/{currentUserId}/unfollow/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable String currentUserId,
            @PathVariable String targetUserId) {
        userService.unfollowUser(currentUserId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Unfollowed successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats() {
        long total = userService.getTotalUsers();
        long active = userService.getActiveUsers();
        long inactive = userService.getInactiveUsers();
        
        UserStats stats = new UserStats(total, active, inactive);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    public static class UserStats {
        private long total;
        private long active;
        private long inactive;

        public UserStats(long total, long active, long inactive) {
            this.total = total;
            this.active = active;
            this.inactive = inactive;
        }

        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getInactive() { return inactive; }
    }
}