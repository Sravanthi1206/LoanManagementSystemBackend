package com.lms.identity.service;

import com.lms.identity.dto.*;
import com.lms.identity.entity.User;
import com.lms.identity.exception.DuplicateUserException;
import com.lms.identity.exception.InvalidCredentialsException;
import com.lms.identity.exception.UserNotFoundException;
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
                .role(User.Role.CUSTOMER) // Always CUSTOMER for public registration - security enforcement
                .active(true)
                .build();

        User savedUser = repository.save(user);
        
        String accessToken = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        UserResponse userResponse = mapToUserResponse(savedUser);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userResponse)
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());
        
        if (!user.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        UserResponse userResponse = mapToUserResponse(user);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userResponse)
                .build();
    }
    
    // Keep backward compatibility with old AuthRequest/AuthResponse
    @Deprecated
    public AuthResponse saveUser(AuthRequest request) {
        UserRegisterRequest newRequest = UserRegisterRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .panCard(request.getPanCard())
                .role(request.getRole())
                .build();
        LoginResponse response = register(newRequest);
        return new AuthResponse(response.getAccessToken(), response.getUser().getRole(), response.getUser().getFirstName());
    }
    
    @Deprecated
    public AuthResponse login(AuthRequest request) {
        LoginRequest newRequest = LoginRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        LoginResponse response = login(newRequest);
        return new AuthResponse(response.getAccessToken(), response.getUser().getRole(), response.getUser().getFirstName());
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
