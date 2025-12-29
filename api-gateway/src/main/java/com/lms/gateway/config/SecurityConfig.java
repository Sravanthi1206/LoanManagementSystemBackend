package com.lms.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // Disable CSRF for REST APIs (stateless)
            .csrf(csrf -> csrf.disable())
            // Disable default login form
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            // Permit all requests - JWT validation is handled by AuthenticationFilter
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/auth/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().permitAll()  // Let AuthenticationFilter handle JWT validation
            );
        
        return http.build();
    }
}
