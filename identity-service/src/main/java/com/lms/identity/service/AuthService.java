package com.lms.identity.service;

import com.lms.identity.dto.*;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidCredentialsException;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse register(UserRegisterRequest request) {
        validateUniqueFields(request);
        
        User user = buildUserFromRequest(request);
        User savedUser = repository.save(user);
        
        return buildLoginResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);
        
        validateUserCredentials(user, request.getPassword());
        
        return buildLoginResponse(user);
    }
    
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new com.lms.identity.exception.UserNotFoundException(userId));
                
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid old password");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        repository.save(user);
    }
    
    private void validateUniqueFields(UserRegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email", request.getEmail());
        }
        if (request.getPanCard() != null && repository.existsByPanCard(request.getPanCard())) {
            throw new DuplicateUserException("PAN card", request.getPanCard());
        }
    }
    
    private void validateUserCredentials(User user, String rawPassword) {
        if (!user.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
    }
    
    private User buildUserFromRequest(UserRegisterRequest request) {
        return User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .panCard(request.getPanCard())
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();
    }
    
    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtService.generateToken(
                user.getEmail(), user.getRole().name(), user.getId());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserResponse(user))
                .build();
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
