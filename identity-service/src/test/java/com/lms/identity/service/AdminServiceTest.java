package com.lms.identity.service;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidRoleException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Service Tests")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("Create Staff Account - success")
    void createStaffAccount_Success() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setEmail("officer@example.com");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setRole(User.Role.LOAN_OFFICER);

        when(userRepository.existsByEmail("officer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = adminService.createStaffAccount(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(User.Role.LOAN_OFFICER, response.getRole());
        assertTrue(response.getPasswordChangeRequired());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create Staff Account - invalid role")
    void createStaffAccount_InvalidRole() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setRole(User.Role.CUSTOMER);

        assertThrows(InvalidRoleException.class, () -> adminService.createStaffAccount(request));
    }

    @Test
    @DisplayName("Create Staff Account - duplicate email")
    void createStaffAccount_DuplicateEmail() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setEmail("officer@example.com");
        request.setRole(User.Role.LOAN_OFFICER);

        when(userRepository.existsByEmail("officer@example.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> adminService.createStaffAccount(request));
    }

    @Test
    @DisplayName("Deactivate User - success")
    void deactivateUser_Success() {
        User user = User.builder().id(1L).active(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = adminService.deactivateUser(1L);

        assertFalse(response.getActive());
        verify(userRepository).save(user);
    }
    
    @Test
    @DisplayName("Activate User - success")
    void activateUser_Success() {
        User user = User.builder().id(1L).active(false).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = adminService.activateUser(1L);

        assertTrue(response.getActive());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deactivate User - not found")
    void deactivateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.deactivateUser(1L));
    }
}
