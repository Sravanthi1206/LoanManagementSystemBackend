package com.lms.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lms.identity.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String email;
    private String password;
    
    // For Registration only
    private String firstName;
    private String lastName;
    private String phone;
    private String panCard;
    private User.Role role; // Optional, default to CUSTOMER if null
}
