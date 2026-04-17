package org.example.nexora.auth;

import org.example.nexora.auth.dto.*;
import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

    public AuthService(UserRepository userRepository, JwtService jwtService,
                     PasswordEncoder passwordEncoder, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegistrationRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered", "EMAIL_EXISTS");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already taken", "USERNAME_EXISTS");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmailVerified(false);
        user.setActive(true);

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        
        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
            "refresh:" + user.getId(),
            refreshToken,
            Duration.ofMillis(refreshExpiration)
        );

        return new AuthResponse(accessToken, refreshToken, user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("Invalid credentials", "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid credentials", "INVALID_CREDENTIALS");
        }

        if (!user.isActive()) {
            throw new BusinessException("Account is disabled", "ACCOUNT_DISABLED");
        }

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
            "refresh:" + user.getId(),
            refreshToken,
            Duration.ofMillis(refreshExpiration)
        );

        return new AuthResponse(accessToken, refreshToken, user.getId());
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token", "INVALID_TOKEN");
        }

        String userId = jwtService.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException("Invalid refresh token", "INVALID_TOKEN");
        }
        String storedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException("Token revoked", "TOKEN_REVOKED");
        }

        long uid;
        try {
            uid = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid refresh token", "INVALID_TOKEN");
        }

        User user = userRepository.findById(uid)
            .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        // Update refresh token in Redis
        redisTemplate.opsForValue().set(
            "refresh:" + user.getId(),
            newRefreshToken,
            Duration.ofMillis(refreshExpiration)
        );

        return new AuthResponse(newAccessToken, newRefreshToken, user.getId());
    }

    public void logout(String token) {
        String userId = jwtService.getUserIdFromToken(token);
        if (userId != null) {
            redisTemplate.delete("refresh:" + userId);
        }
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new BusinessException("Invalid token", "INVALID_TOKEN"));
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Email not found", "EMAIL_NOT_FOUND"));

        if (user.isEmailVerified()) {
            throw new BusinessException("Email already verified", "ALREADY_VERIFIED");
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        userRepository.save(user);
        
        // TODO: Send verification email
    }

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Email not found", "EMAIL_NOT_FOUND"));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        userRepository.save(user);
        
        // TODO: Send password reset email
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
            .orElseThrow(() -> new BusinessException("Invalid token", "INVALID_TOKEN"));

        validatePassword(request.getNewPassword());
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        userRepository.save(user);

        // Invalidate all refresh tokens
        redisTemplate.delete("refresh:" + user.getId());
    }

    public void changePassword(String token, ChangePasswordRequest request) {
        String userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException("Invalid token", "INVALID_TOKEN");
        }
        long uid;
        try {
            uid = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid token", "INVALID_TOKEN");
        }
        User user = userRepository.findById(uid)
            .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect", "INVALID_PASSWORD");
        }

        validatePassword(request.getNewPassword());
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all refresh tokens
        redisTemplate.delete("refresh:" + user.getId());
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException("Email is required", "EMAIL_REQUIRED");
        }
        
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new BusinessException("Invalid email format", "INVALID_EMAIL");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("Password is required", "PASSWORD_REQUIRED");
        }

        validatePassword(request.getPassword());
    }

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException(
                "Password must be at least 8 characters with uppercase, lowercase and number",
                "WEAK_PASSWORD"
            );
        }
    }
}