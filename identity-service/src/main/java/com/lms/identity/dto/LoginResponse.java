package com.lms.identity.dto;

import com.lms.identity.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
    
    public LoginResponse(String accessToken, UserResponse user) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = 86400L; // 24 hours
        this.user = user;
    }
}
