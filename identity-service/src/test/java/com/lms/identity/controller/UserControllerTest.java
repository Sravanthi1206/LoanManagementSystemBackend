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

        @Test
        @DisplayName("GET /users/officers - Get all officers")
        void getOfficersShouldSucceed() throws Exception {
            UserResponse officerResponse = UserResponse.builder()
                    .id(2L)
                    .email("officer@example.com")
                    .firstName("Officer")
                    .lastName("User")
                    .role(User.Role.LOAN_OFFICER)
                    .active(true)
                    .build();
            when(userService.getOfficers()).thenReturn(Arrays.asList(officerResponse));

            mockMvc.perform(get("/users/officers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].role").value("LOAN_OFFICER"));
        }
    }

    @Nested
    @DisplayName("Credit Score Tests")
    class CreditScoreTests {

        @Test
        @DisplayName("GET /users/{id}/credit-score - Success")
        void getCreditScoreShouldSucceed() throws Exception {
            when(creditScoreService.getCreditScore(1L)).thenReturn(750);

            mockMvc.perform(get("/users/1/credit-score"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.creditScore").value(750));
        }

        @Test
        @DisplayName("POST /users/{id}/credit-score/increment - Success")
        void incrementCreditScoreShouldSucceed() throws Exception {
            when(creditScoreService.incrementCreditScore(1L)).thenReturn(755);

            mockMvc.perform(post("/users/1/credit-score/increment"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.creditScore").value(755));
        }

        @Test
        @DisplayName("PUT /users/{id}/credit-score - Success")
        void setCreditScoreShouldSucceed() throws Exception {
            when(creditScoreService.setCreditScore(eq(1L), eq(800))).thenReturn(800);

            mockMvc.perform(put("/users/1/credit-score")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content("{\"creditScore\": 800}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.creditScore").value(800));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("PUT /users/{id} - Success")
        void updateUserShouldSucceed() throws Exception {
            UserRegisterRequest updateRequest = new UserRegisterRequest();
            updateRequest.setFirstName("Updated");
            updateRequest.setLastName("Name");
            updateRequest.setEmail(TEST_EMAIL);
            updateRequest.setPassword("NewPass@123");
            updateRequest.setPhone("+919876543210");

            UserResponse updatedResponse = UserResponse.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .firstName("Updated")
                    .lastName("Name")
                    .role(User.Role.CUSTOMER)
                    .active(true)
                    .build();

            when(userService.updateUser(eq(1L), any(UserRegisterRequest.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/users/1")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"));
        }

        @Test
        @DisplayName("DELETE /users/{id} - Deactivate Success")
        void deactivateUserShouldSucceed() throws Exception {
            doNothing().when(userService).deactivateUser(1L);

            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
