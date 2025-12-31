package com.lms.identity.service;

import com.lms.identity.dto.CreateStaffRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidRoleException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin operations - managing staff accounts.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a staff account (LOAN_OFFICER or ADMIN only).
     * CUSTOMER accounts must use public registration.
     */
    @Transactional
    public UserResponse createStaffAccount(CreateStaffRequest request) {
        // Validate role - only staff roles allowed via this endpoint
        if (request.getRole() == User.Role.CUSTOMER) {
            throw new InvalidRoleException("Customer accounts must be created via public registration");
        }
        
        // Check for duplicate email
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .build();

        User savedUser = repository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Deactivate a user account.
     */
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setActive(false);
        User savedUser = repository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Activate a user account.
     */
    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setActive(true);
        User savedUser = repository.save(user);
        return mapToUserResponse(savedUser);
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
                .createdAt(user.getCreatedAt())
                .build();
    }
}
