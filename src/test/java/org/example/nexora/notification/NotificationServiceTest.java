package org.example.nexora.notification;

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
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testGetAllNotifications() {
        when(notificationRepository.findAll()).thenReturn(Arrays.asList());
        List result = notificationService.getAllNotifications();
        assertNotNull(result);
    }
}