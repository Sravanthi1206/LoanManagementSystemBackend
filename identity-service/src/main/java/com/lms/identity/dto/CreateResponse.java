package com.lms.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for resource creation endpoints.
 * Returns only the ID of the created resource.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateResponse {
    private Long id;
}
