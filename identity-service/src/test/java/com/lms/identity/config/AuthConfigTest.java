package com.lms.identity.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("Auth Config Tests")
class AuthConfigTest {

    private final AuthConfig config = new AuthConfig();

    @Test
    @DisplayName("Bean creation")
    void testBeanCreation() throws Exception {
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);

        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        config.authenticationManager(authConfig);
        verify(authConfig, times(1)).getAuthenticationManager();
    }
}
