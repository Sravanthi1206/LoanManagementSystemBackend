package com.lms.identity.controller;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("Admin Controller Tests")
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("Create Staff Account - success")
    void createStaffAccount() throws Exception {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setPhone("+1234567890");
        request.setRole(User.Role.ADMIN);

        UserResponse response = UserResponse.builder().id(1L).email("admin@example.com").role(User.Role.ADMIN).build();
        when(adminService.createStaffAccount(any(CreateStaffRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Deactivate User")
    void deactivateUser() throws Exception {
        UserResponse response = UserResponse.builder().id(1L).active(false).build();
        when(adminService.deactivateUser(any(Long.class), any())).thenReturn(response);

        mockMvc.perform(put("/admin/users/1/status?active=false")
                        .header("X-User-Id", "99")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Activate User")
    void activateUser() throws Exception {
        UserResponse response = UserResponse.builder().id(1L).active(true).build();
        when(adminService.activateUser(any(Long.class), any())).thenReturn(response);

        mockMvc.perform(put("/admin/users/1/status?active=true")
                        .header("X-User-Id", "99")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Nested
    @DisplayName("Root Admin Endpoints Tests")
    @org.springframework.security.test.context.support.WithMockUser(roles = "ROOT_ADMIN")
    class RootAdminTests {

        @Test
        @DisplayName("Get Pending Approvals - Success")
        void getPendingApprovals() throws Exception {
            UserResponse pendingUser = UserResponse.builder()
                    .id(2L)
                    .email("pending@example.com")
                    .role(User.Role.ADMIN)
                    .active(false)
                    .build();
            when(adminService.getPendingApprovals()).thenReturn(java.util.List.of(pendingUser));

            mockMvc.perform(get("/admin/pending-approvals")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].email").value("pending@example.com"));
        }

        @Test
        @DisplayName("Approve Admin - Success")
        void approveAdmin() throws Exception {
            UserResponse approvedUser = UserResponse.builder()
                    .id(2L)
                    .email("approved@example.com")
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            when(adminService.approveAdmin(2L)).thenReturn(approvedUser);

            mockMvc.perform(put("/admin/approve/2")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Reject Admin - Success")
        void rejectAdmin() throws Exception {
            doNothing().when(adminService).rejectAdmin(2L);

            mockMvc.perform(delete("/admin/reject/2")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isNoContent());
        }
    }
}
