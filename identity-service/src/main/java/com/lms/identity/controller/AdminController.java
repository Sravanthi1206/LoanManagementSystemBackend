package com.lms.identity.controller;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.CreateResponse;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users")
    public ResponseEntity<CreateResponse> createStaffAccount(@Valid @RequestBody CreateStaffRequest request) {
        UserResponse response = adminService.createStaffAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateResponse.builder().id(response.getId()).build());
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.deactivateUser(userId));
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.activateUser(userId));
    }
}
