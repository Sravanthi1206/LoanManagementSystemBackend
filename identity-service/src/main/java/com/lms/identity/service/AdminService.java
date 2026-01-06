package com.lms.identity.service;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidRoleException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createStaffAccount(CreateStaffRequest request, Long creatorUserId) {
        if (request.getRole() == User.Role.CUSTOMER) {
            throw new InvalidRoleException("Customer accounts must be created via public registration");
        }
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }
        
        // Check if creator is ROOT_ADMIN - if so, approve immediately
        // Otherwise, if creating an ADMIN, set pending approval
        boolean needsApproval = false;
        if (request.getRole() == User.Role.ADMIN && creatorUserId != null) {
            User creator = repository.findById(creatorUserId).orElse(null);
            if (creator != null && creator.getRole() != User.Role.ROOT_ADMIN) {
                needsApproval = true;
            }
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(!needsApproval)  // Inactive until approved if needs approval
                .approved(!needsApproval)
                .approvalPending(needsApproval)
                .createdByUserId(creatorUserId)
                .passwordChangeRequired(true)
                .build();

        return mapToUserResponse(repository.save(user));
    }
    
    // Overload for backward compatibility
    @Transactional
    public UserResponse createStaffAccount(CreateStaffRequest request) {
        return createStaffAccount(request, null);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // ROOT_ADMIN cannot be deactivated
        if (user.getRole() == User.Role.ROOT_ADMIN) {
            throw new InvalidRoleException("ROOT_ADMIN cannot be deactivated");
        }
        
        user.setActive(false);
        return mapToUserResponse(repository.save(user));
    }

    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setActive(true);
        return mapToUserResponse(repository.save(user));
    }
    
    // Get pending admin approvals
    public List<UserResponse> getPendingApprovals() {
        return repository.findByApprovalPendingTrue().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    // Approve pending admin (ROOT_ADMIN only)
    @Transactional
    public UserResponse approveAdmin(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.getApprovalPending()) {
            throw new InvalidRoleException("User is not pending approval");
        }
        
        user.setApproved(true);
        user.setApprovalPending(false);
        user.setActive(true);
        
        return mapToUserResponse(repository.save(user));
    }
    
    // Reject pending admin (ROOT_ADMIN only)
    @Transactional
    public void rejectAdmin(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.getApprovalPending()) {
            throw new InvalidRoleException("User is not pending approval");
        }
        
        repository.delete(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .active(user.getActive())
                .approved(user.getApproved())
                .approvalPending(user.getApprovalPending())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
