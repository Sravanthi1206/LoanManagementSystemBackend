package com.lms.identity.service;

import com.lms.identity.dto.UserRegisterRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();

        registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("pass");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID")
        void getUserById_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testUser));

            UserResponse result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(testUser.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void getUserById_NotFound_ThrowsException() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
        }

        @Test
        @DisplayName("Should get user by email")
        void getUserByEmail_Success() {
            when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            UserResponse result = userService.getUserByEmail("test@example.com");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should get all users")
        void getAllUsers_Success() {
            Page<User> page = new PageImpl<>(Collections.singletonList(testUser));
            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            Page<UserResponse> result = userService.getAllUsers(Pageable.unpaged());

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_Success() {
            when(repository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(repository.save(any(User.class))).thenReturn(testUser);

            UserResponse result = userService.createUser(registerRequest);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw if email exists")
        void createUser_EmailExists() {
            when(repository.existsByEmail(anyString())).thenReturn(true);

            assertThrows(DuplicateUserException.class, () -> userService.createUser(registerRequest));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user")
        void updateUser_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testUser));
            when(repository.save(any(User.class))).thenReturn(testUser);

            UserResponse result = userService.updateUser(1L, registerRequest);

            assertNotNull(result);
            verify(repository).save(any(User.class));
        }

        @Test
        @DisplayName("Should deactivate user")
        void deactivateUser_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testUser));
            when(repository.save(any(User.class))).thenReturn(testUser);

            userService.deactivateUser(1L);

            verify(repository).save(argThat(u -> !u.getActive()));
        }
    }
}
