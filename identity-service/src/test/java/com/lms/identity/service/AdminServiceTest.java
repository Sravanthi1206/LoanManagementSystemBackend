package com.lms.identity.service;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidRoleException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.messaging.NotificationPublisher;
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

    @Mock
    private NotificationPublisher notificationPublisher;

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

    // ROOT_ADMIN Approval Workflow Tests

    @Test
    @DisplayName("Create Admin by non-ROOT_ADMIN requires approval")
    void createAdminByNonRootAdmin_RequiresApproval() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password");
        request.setFirstName("New");
        request.setLastName("Admin");
        request.setRole(User.Role.ADMIN);

        User creator = User.builder().id(10L).role(User.Role.ADMIN).build();
        
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = adminService.createStaffAccount(request, 10L);

        assertNotNull(response);
        assertTrue(response.getApprovalPending());
        assertFalse(response.getApproved());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create Admin by ROOT_ADMIN does not require approval")
    void createAdminByRootAdmin_NoApprovalNeeded() {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password");
        request.setFirstName("New");
        request.setLastName("Admin");
        request.setRole(User.Role.ADMIN);

        User creator = User.builder().id(1L).role(User.Role.ROOT_ADMIN).build();
        
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = adminService.createStaffAccount(request, 1L);

        assertNotNull(response);
        assertFalse(response.getApprovalPending());
        assertTrue(response.getApproved());
    }

    @Test
    @DisplayName("Get Pending Approvals - returns pending users")
    void getPendingApprovals_ReturnsPendingUsers() {
        User pendingUser = User.builder()
                .id(5L)
                .email("pending@example.com")
                .approvalPending(true)
                .approved(false)
                .role(User.Role.ADMIN)
                .build();
        
        when(userRepository.findByApprovalPendingTrue()).thenReturn(java.util.List.of(pendingUser));

        var result = adminService.getPendingApprovals();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertTrue(result.get(0).getApprovalPending());
    }

    @Test
    @DisplayName("Approve Admin - success")
    void approveAdmin_Success() {
        User pendingUser = User.builder()
                .id(5L)
                .email("pending@example.com")
                .approvalPending(true)
                .approved(false)
                .active(false)
                .role(User.Role.ADMIN)
                .build();
        
        when(userRepository.findById(5L)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = adminService.approveAdmin(5L);

        assertFalse(response.getApprovalPending());
        assertTrue(response.getApproved());
        assertTrue(response.getActive());
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("Approve Admin - user not pending throws exception")
    void approveAdmin_NotPending_ThrowsException() {
        User activeUser = User.builder()
                .id(5L)
                .approvalPending(false)
                .approved(true)
                .build();
        
        when(userRepository.findById(5L)).thenReturn(Optional.of(activeUser));

        assertThrows(InvalidRoleException.class, () -> adminService.approveAdmin(5L));
    }

    @Test
    @DisplayName("Reject Admin - success")
    void rejectAdmin_Success() {
        User pendingUser = User.builder()
                .id(5L)
                .approvalPending(true)
                .build();
        
        when(userRepository.findById(5L)).thenReturn(Optional.of(pendingUser));

        adminService.rejectAdmin(5L);

        verify(userRepository).delete(pendingUser);
    }

    @Test
    @DisplayName("Reject Admin - user not pending throws exception")
    void rejectAdmin_NotPending_ThrowsException() {
        User activeUser = User.builder()
                .id(5L)
                .approvalPending(false)
                .build();
        
        when(userRepository.findById(5L)).thenReturn(Optional.of(activeUser));

        assertThrows(InvalidRoleException.class, () -> adminService.rejectAdmin(5L));
    }

    @Test
    @DisplayName("Reject Admin - user not found throws exception")
    void rejectAdmin_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.rejectAdmin(99L));
    }
}
