package org.example.nexora.messaging;

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
class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;
    @InjectMocks
    private MessageService messageService;

    @Test
    void testGetAllMessages() {
        when(messageRepository.findAll()).thenReturn(Arrays.asList());
        List result = messageService.getAllMessages();
        assertNotNull(result);
    }
}