package org.example.nexora.grocery;

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
class GroceryServiceTest {
    @Mock
    private GroceryRepository groceryRepository;
    @InjectMocks
    private GroceryService groceryService;

    @Test
    void testGetAllGroceryItems() {
        when(groceryRepository.findAll()).thenReturn(Arrays.asList());
        List result = groceryService.getAllGroceryItems();
        assertNotNull(result);
    }
}