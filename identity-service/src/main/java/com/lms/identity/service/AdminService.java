package com.lms.identity.service;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidRoleException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.messaging.NotificationPublisher;
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
    private final NotificationPublisher notificationPublisher;

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

        User savedUser = repository.save(user);
        
        // Send credentials email to the new staff member
        try {
            notificationPublisher.sendCredentialsNotification(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getPassword(),
                    request.getRole().name()
            );
        } catch (Exception e) {
            // Log but don't fail - account is created successfully
        }
        
        return mapToUserResponse(savedUser);
    }
    
    // Overload for backward compatibility
    @Transactional
    public UserResponse createStaffAccount(CreateStaffRequest request) {
        return createStaffAccount(request, null);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId, Long requestorId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // ROOT_ADMIN cannot be deactivated
        if (user.getRole() == User.Role.ROOT_ADMIN) {
            throw new InvalidRoleException("ROOT_ADMIN cannot be deactivated");
        }
        
        // Only ROOT_ADMIN can deactivate other ADMINs
        if (user.getRole() == User.Role.ADMIN && requestorId != null) {
            User requestor = repository.findById(requestorId).orElse(null);
            if (requestor != null && requestor.getRole() != User.Role.ROOT_ADMIN) {
                throw new InvalidRoleException("Only ROOT_ADMIN can deactivate ADMIN accounts");
            }
        }
        
        user.setActive(false);
        User savedUser = repository.save(user);
        
        // Send deactivation notification
        try {
            notificationPublisher.sendAccountDeactivatedNotification(
                    user.getEmail(),
                    user.getFirstName(),
                    user.getRole().name()
            );
        } catch (Exception e) {
            // Log but don't fail
        }
        
        return mapToUserResponse(savedUser);
    }
    
    // Overload for backward compatibility
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        return deactivateUser(userId, null);
    }

    @Transactional
    public UserResponse activateUser(Long userId, Long requestorId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Only ROOT_ADMIN can activate other ADMINs
        if (user.getRole() == User.Role.ADMIN && requestorId != null) {
            User requestor = repository.findById(requestorId).orElse(null);
            if (requestor != null && requestor.getRole() != User.Role.ROOT_ADMIN) {
                throw new InvalidRoleException("Only ROOT_ADMIN can activate ADMIN accounts");
            }
        }
        
        user.setActive(true);
        User savedUser = repository.save(user);
        
        // Send activation notification
        try {
            notificationPublisher.sendAccountActivatedNotification(
                    user.getEmail(),
                    user.getFirstName(),
                    user.getRole().name()
            );
        } catch (Exception e) {
            // Log but don't fail
        }
        
        return mapToUserResponse(savedUser);
    }
    
    // Overload for backward compatibility
    @Transactional
    public UserResponse activateUser(Long userId) {
        return activateUser(userId, null);
    }
    
    // Get pending admin approvals
    public List<UserResponse> getPendingApprovals() {
        return repository.findByApprovalPendingTrue().stream()
                .map(this::mapToUserResponse)
                .toList();
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
                .panCard(user.getPanCard())
                .role(user.getRole())
                .active(user.getActive())
                .approved(user.getApproved())
                .approvalPending(user.getApprovalPending())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
