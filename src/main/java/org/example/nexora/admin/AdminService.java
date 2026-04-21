package org.example.nexora.admin;

import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.user.UserRole;
import org.example.nexora.video.Video;
import org.example.nexora.video.VideoRepository;
import org.example.nexora.wallet.WithdrawRepository;
import org.example.nexora.wallet.WithdrawRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final WithdrawRepository withdrawRepository;

    public AdminService(UserRepository userRepository,
                        VideoRepository videoRepository,
                        WithdrawRepository withdrawRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.withdrawRepository = withdrawRepository;
    }

    // 👤 ALL USERS
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // 🎥 DELETE VIDEO
    public String deleteVideo(Long videoId) {
        videoRepository.deleteById(videoId);
        return "Video deleted";
    }

    // 💳 VIEW WITHDRAW REQUESTS
    public List<WithdrawRequest> getWithdraws() {
        return withdrawRepository.findAll();
    }

    // ✅ APPROVE WITHDRAW
    public String approveWithdraw(Long id) {

        WithdrawRequest req = withdrawRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        req.setStatus("APPROVED");
        withdrawRepository.save(req);

        return "Withdraw approved";
    }

    // ❌ REJECT WITHDRAW
    public String rejectWithdraw(Long id) {

        WithdrawRequest req = withdrawRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        req.setStatus("REJECTED");
        withdrawRepository.save(req);

        return "Withdraw rejected";
    }
    
    // 📊 GET DASHBOARD STATS
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalVideos", videoRepository.count());
        stats.put("totalWithdraws", withdrawRepository.count());
        stats.put("pendingWithdraws", withdrawRepository.findAll().stream()
                .filter(w -> "PENDING".equals(w.getStatus()))
                .count());
        return stats;
    }
    
    // 👤 UPDATE USER ROLE
    public String updateUserRole(String userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        return "User role updated to " + role;
    }
}