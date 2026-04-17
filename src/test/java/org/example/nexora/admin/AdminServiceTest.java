package org.example.nexora.admin;

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
class AdminServiceTest {
    @Mock
    private org.example.nexora.admin.AdminRepository adminRepository;
    @InjectMocks
    private AdminService adminService;

    @Test
    void testGetAllAdmins() {
        when(adminRepository.findAll()).thenReturn(Arrays.asList());
        List result = adminService.getAllAdmins();
        assertNotNull(result);
    }
}