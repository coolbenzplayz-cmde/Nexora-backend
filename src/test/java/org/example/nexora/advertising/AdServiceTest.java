package org.example.nexora.advertising;

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
class AdServiceTest {
    @Mock
    private AdRepository adRepository;
    @InjectMocks
    private AdService adService;

    @Test
    void testGetAllAds() {
        when(adRepository.findAll()).thenReturn(Arrays.asList());
        List result = adService.getAllAds();
        assertNotNull(result);
    }
}