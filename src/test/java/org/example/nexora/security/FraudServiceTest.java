package org.example.nexora.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudServiceTest {
    @Mock
    private FraudLimitService fraudLimitService;
    @InjectMocks
    private FraudService fraudService;

    @Test
    void testCheckFraud() {
        when(fraudLimitService.getAllLimits()).thenReturn(Arrays.asList());
        List result = fraudService.checkFraud("user123");
        assertNotNull(result);
    }
}