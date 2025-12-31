package com.lms.identity.service;

import com.lms.identity.dto.UserRegisterRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public UserResponse createUser(UserRegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }
        if (request.getPanCard() != null && repository.existsByPanCard(request.getPanCard())) {
            throw new DuplicateUserException("PAN card", request.getPanCard());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .panCard(request.getPanCard())
                .role(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER)
                .active(true)
                .build();

        User savedUser = repository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRegisterRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        
        if (request.getPanCard() != null) {
            user.setPanCard(request.getPanCard());
        }

        User updatedUser = repository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        repository.save(user);
    }

    private UserResponse mapToResponse(User user) {
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
