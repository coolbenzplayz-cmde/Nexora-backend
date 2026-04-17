package org.example.nexora.video;

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
class VideoServiceTest {
    @Mock
    private VideoRepository videoRepository;
    @InjectMocks
    private VideoService videoService;

    @Test
    void testGetAllVideos() {
        when(videoRepository.findAll()).thenReturn(Arrays.asList());
        List result = videoService.getAllVideos();
        assertNotNull(result);
    }
}