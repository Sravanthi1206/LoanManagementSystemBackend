package com.lms.identity.service;

import com.lms.identity.dto.*;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidCredentialsException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuthService to achieve high code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserRegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("+919876543210")
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();

        registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");
        registerRequest.setPhone("+919876543211");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password@123");
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {
        
        @Test
        @DisplayName("Should register new user successfully")
        void register_Success() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

            LoginResponse result = authService.register(registerRequest);

            assertNotNull(result);
            assertNotNull(result.getAccessToken());
            assertEquals("Bearer", result.getTokenType());
            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(passwordEncoder).encode(registerRequest.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_DuplicateEmail_ThrowsException() {
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            assertThrows(DuplicateUserException.class, () -> authService.register(registerRequest));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when PAN card already exists")
        void register_DuplicatePanCard_ThrowsException() {
            registerRequest.setPanCard("ABCDE1234F");
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPanCard(anyString())).thenReturn(true);

            assertThrows(DuplicateUserException.class, () -> authService.register(registerRequest));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should register user with all optional fields")
        void register_WithOptionalFields_Success() {
            registerRequest.setPanCard("ABCDE1234F");
            registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPanCard(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

            LoginResponse result = authService.register(registerRequest);

            assertNotNull(result);
            verify(userRepository).existsByPanCard("ABCDE1234F");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        
        @Test
        @DisplayName("Should authenticate user with valid credentials")
        void login_ValidCredentials_ReturnsToken() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

            LoginResponse response = authService.login(loginRequest);

            assertNotNull(response);
            assertEquals("jwt-token", response.getAccessToken());
            assertEquals("Bearer", response.getTokenType());
            verify(jwtService).generateToken(testUser.getEmail(), testUser.getRole().name(), testUser.getId());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void login_UserNotFound_ThrowsException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
            verify(jwtService, never()).generateToken(anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("Should throw exception when account is deactivated")
        void login_InactiveAccount_ThrowsException() {
            testUser.setActive(false);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

            InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class, 
                () -> authService.login(loginRequest)
            );
            assertEquals("Account is deactivated", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when password is invalid")
        void login_InvalidPassword_ThrowsException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
            verify(jwtService, never()).generateToken(anyString(), anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        private ChangePasswordRequest changePasswordRequest;

        @BeforeEach
        void setUp() {
            changePasswordRequest = new ChangePasswordRequest("oldPass", "newPass123");
        }

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPass123")).thenReturn("newEncodedPassword");

            authService.changePassword(1L, changePasswordRequest);

            verify(userRepository).save(testUser);
            assertFalse(testUser.getPasswordChangeRequired());
        }

        @Test
        @DisplayName("Should throw exception when old password invalid")
        void changePassword_InvalidOldPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, 
                () -> authService.changePassword(1L, changePasswordRequest));
            verify(userRepository, never()).save(any(User.class));
        }
    }
}

