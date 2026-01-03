package com.lms.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.dto.NotificationResponse;
import com.lms.notification.entity.Notification;
import com.lms.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Notification Controller Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private NotificationRequest request;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        request = NotificationRequest.builder()
                .userId(1L)
                .loanId(101L)
                .type(Notification.NotificationType.EMAIL)
                .subject("Subject")
                .message("Message")
                .recipient("test@example.com")
                .build();

        response = NotificationResponse.builder()
                .id("notif-1")
                .userId(1L)
                .status(Notification.NotificationStatus.SENT)
                .build();
    }

    @Test
    @DisplayName("POST /notifications/send - Success")
    void sendNotification_Success() throws Exception {
        when(notificationService.sendNotification(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("notif-1"));
    }

    @Test
    @DisplayName("GET /notifications/user/{userId} - Success")
    void getUserNotifications_Success() throws Exception {
        Page<NotificationResponse> page = new PageImpl<>(Collections.singletonList(response));
        when(notificationService.getUserNotifications(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("notif-1"));
    }

    @Test
    @DisplayName("GET /notifications/user/{userId}/unread - Success")
    void getUnreadNotifications_Success() throws Exception {
        List<NotificationResponse> list = Collections.singletonList(response);
        when(notificationService.getUnreadNotifications(1L)).thenReturn(list);

        mockMvc.perform(get("/notifications/user/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("notif-1"));
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read - Success")
    void markAsRead_Success() throws Exception {
        response.setStatus(Notification.NotificationStatus.READ);
        when(notificationService.markAsRead("notif-1")).thenReturn(response);

        mockMvc.perform(put("/notifications/notif-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }
}
