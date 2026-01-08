package com.lms.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.identity.dto.*;
import com.lms.identity.entity.User;
import com.lms.identity.service.CreditScoreService;
import com.lms.identity.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive controller tests for UserController.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Tests")
class UserControllerTest {

    private static final String TEST_EMAIL = "test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    
    @MockBean
    private CreditScoreService creditScoreService;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("GET /users/profile - Success")
        void getProfileShouldSucceed() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(userResponse);

            mockMvc.perform(get("/users/profile")
                    .header("X-User-Email", TEST_EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL));
        }

        @Test
        @DisplayName("GET /users/{id} - Success")
        void getUserByIdShouldSucceed() throws Exception {
            when(userService.getUserById(1L)).thenReturn(userResponse);

            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("GET /users - Get all users")
        void getAllUsersShouldSucceed() throws Exception {
            Page<UserResponse> users = new PageImpl<>(Arrays.asList(userResponse));
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(users);

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1));
        }
    }
}
