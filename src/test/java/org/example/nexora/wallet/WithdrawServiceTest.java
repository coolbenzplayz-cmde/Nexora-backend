package org.example.nexora.wallet;

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
class WithdrawServiceTest {
    @Mock
    private WithdrawRepository withdrawRepository;
    @InjectMocks
    private WithdrawService withdrawService;

    @Test
    void testGetAllWithdrawals() {
        when(withdrawRepository.findAll()).thenReturn(Arrays.asList());
        List result = withdrawService.getAllWithdrawRequests();
        assertNotNull(result);
    }
}