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


@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createStaffAccount(CreateStaffRequest request) {
        if (request.getRole() == User.Role.CUSTOMER) {
            throw new InvalidRoleException("Customer accounts must be created via public registration");
        }
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
                .passwordChangeRequired(true)
                .build();

        return mapToUserResponse(repository.save(user));
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
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
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
