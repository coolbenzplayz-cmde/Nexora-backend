package org.example.nexora.payment;

import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.wallet.Wallet;
import org.example.nexora.wallet.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentFailsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(1L, BigDecimal.ONE, "PAYMENT", "x"));
    }

    @Test
    void processPaymentFailsWhenInsufficientBalance() {
        User user = new User();
        user.setId(1L);
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(1L, BigDecimal.TEN, "PAYMENT", "x"));
    }
}
