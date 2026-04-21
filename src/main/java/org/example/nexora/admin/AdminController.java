package org.example.nexora.admin;

import org.example.nexora.common.BusinessException;
import org.example.nexora.security.JwtService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.user.UserRole;
import org.example.nexora.video.Video;
import org.example.nexora.wallet.WithdrawRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, JwtService jwtService, UserRepository userRepository) {
        this.adminService = adminService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // Helper method to verify admin access - only owner user can access
    private void verifyAdminAccess(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Unauthorized: No token provided");
        }
        
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new BusinessException("Unauthorized: User not found");
        }
        
        User user = userOpt.get();
        // Only allow ADMIN role AND the specific owner user
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException("Forbidden: Admin access required");
        }
        
        // Additional check: verify this is the owner (you can configure this)
        // For now, any ADMIN can access. To restrict to specific user, check username:
        // if (!"admin".equals(username) && !"yourusername".equals(username)) {
        //     throw new BusinessException("Forbidden: Only owner can access admin panel");
        // }
    }

    // 👤 USERS - Admin only
    @GetMapping("/users")
    public List<User> users(HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.getUsers();
    }

    // 🎥 DELETE VIDEO - Admin only
    @DeleteMapping("/video/{id}")
    public String deleteVideo(@PathVariable Long id, HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.deleteVideo(id);
    }

    // 💳 WITHDRAW REQUESTS - Admin only
    @GetMapping("/withdraws")
    public List<WithdrawRequest> withdraws(HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.getWithdraws();
    }

    // ✅ APPROVE - Admin only
    @PostMapping("/withdraw/approve/{id}")
    public String approve(@PathVariable Long id, HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.approveWithdraw(id);
    }

    // ❌ REJECT - Admin only
    @PostMapping("/withdraw/reject/{id}")
    public String reject(@PathVariable Long id, HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.rejectWithdraw(id);
    }
    
    // 📊 DASHBOARD STATS - Admin only
    @GetMapping("/stats")
    public Object getStats(HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.getStats();
    }
    
    // 👤 UPDATE USER ROLE - Admin only
    @PostMapping("/user/{id}/role")
    public String updateUserRole(@PathVariable String id, @RequestParam Role role, HttpServletRequest request) {
        verifyAdminAccess(request);
        return adminService.updateUserRole(id, role);
    }
}