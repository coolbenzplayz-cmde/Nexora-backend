package org.example.nexora.social;

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
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private PostService postService;

    @Test
    void testGetAllPosts() {
        when(postRepository.findAll()).thenReturn(Arrays.asList());
        List result = postService.getAllPosts();
        assertNotNull(result);
    }
}