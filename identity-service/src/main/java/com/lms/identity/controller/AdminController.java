package com.lms.identity.controller;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for managing staff accounts.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Create a new staff account (LOAN_OFFICER or ADMIN).
     * Only accessible by users with ADMIN role.
     */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createStaffAccount(@Valid @RequestBody CreateStaffRequest request) {
        UserResponse response = adminService.createStaffAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deactivate a user account.
     */
    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long userId) {
        UserResponse response = adminService.deactivateUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a user account.
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        UserResponse response = adminService.activateUser(userId);
        return ResponseEntity.ok(response);
    }
}
