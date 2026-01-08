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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ROOT_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users")
    public ResponseEntity<CreateResponse> createStaffAccount(@Valid @RequestBody CreateStaffRequest request) {
        Long creatorId = getCurrentUserId();
        UserResponse response = adminService.createStaffAccount(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateResponse.builder().id(response.getId()).build());
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam("active") boolean active,
            @RequestHeader(value = "X-User-Id", required = false) Long requestorId) {
        if (active) {
            return ResponseEntity.ok(adminService.activateUser(userId, requestorId));
        } else {
            return ResponseEntity.ok(adminService.deactivateUser(userId, requestorId));
        }
    }
    
    // ROOT_ADMIN only endpoints
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ResponseEntity<List<UserResponse>> getPendingApprovals() {
        return ResponseEntity.ok(adminService.getPendingApprovals());
    }
    
    @PutMapping("/approve/{userId}")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ResponseEntity<UserResponse> approveAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveAdmin(userId));
    }
    
    @DeleteMapping("/reject/{userId}")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ResponseEntity<Void> rejectAdmin(@PathVariable Long userId) {
        adminService.rejectAdmin(userId);
        return ResponseEntity.noContent().build();
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // The principal contains the user ID in the name/username field
            String username = ((org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal()).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
