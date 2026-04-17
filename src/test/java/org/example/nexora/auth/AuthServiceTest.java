package org.example.nexora.auth;

import org.example.nexora.auth.dto.LoginRequest;
import org.example.nexora.common.BusinessException;
import org.example.nexora.security.JwtService;
import org.example.nexora.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @InjectMocks
    private AuthService authService;

    @Test
    void loginFailsWhenUserMissing() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("Secret123");
        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
