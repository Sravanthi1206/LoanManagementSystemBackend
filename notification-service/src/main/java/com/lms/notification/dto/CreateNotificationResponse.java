package com.lms.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for notification creation endpoints.
 * Returns only the ID of the created notification.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationResponse {
    private String id;
}
