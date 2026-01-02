package com.lms.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.identity.dto.LoginRequest;
import com.lms.identity.dto.LoginResponse;
import com.lms.identity.dto.UserRegisterRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private UserRegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhone("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password@123");
    }

    @Test
    @DisplayName("POST /auth/register - Success")
    void register_Success() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.CUSTOMER)
                .build();
        
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("jwt-token-here")
                .user(userResponse)
                .build();

        when(authService.register(any(UserRegisterRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"));
    }

    @Test
    @DisplayName("POST /auth/login - Success")
    void login_Success() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .role(User.Role.CUSTOMER)
                .build();
                
        LoginResponse response = LoginResponse.builder()
                .accessToken("jwt-token-here")
                .user(userResponse)
                .build();
                
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-here"))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    @Test
    @DisplayName("POST /auth/register - Validation Error")
    void register_ValidationError() throws Exception {
        // Arrange - empty request
        UserRegisterRequest invalidRequest = new UserRegisterRequest();

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
