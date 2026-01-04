package com.lms.gateway.filter;

import com.lms.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            
            System.out.println("[DEBUG] Processing request for path: " + path);
            
            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                System.out.println("[DEBUG] Skipping auth for public endpoint: " + path);
                return chain.filter(exchange);
            }

            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                System.out.println("[DEBUG] Missing Authorization header for: " + path);
                return onError(exchange.getResponse(), "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            System.out.println("[DEBUG] Auth header present, length: " + (authHeader != null ? authHeader.length() : 0));
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
                System.out.println("[DEBUG] Token extracted, length: " + authHeader.length());
            } else {
                System.out.println("[DEBUG] Invalid auth header format");
                return onError(exchange.getResponse(), "Invalid Authorization Header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                System.out.println("[DEBUG] Attempting to validate token...");
                jwtUtil.validateToken(authHeader);
                System.out.println("[DEBUG] Token validation successful!");
                
                // Extract email, role, and userId from token and add them as headers for downstream services
                String email = jwtUtil.getEmailFromToken(authHeader);
                String role = jwtUtil.getRoleFromToken(authHeader);
                Long userId = jwtUtil.getUserIdFromToken(authHeader);
                
                System.out.println("[DEBUG] Extracted claims - email: " + email + ", role: " + role + ", userId: " + userId);
                
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                        .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                System.out.println("[DEBUG] Token validation FAILED!");
                System.out.println("[DEBUG] Exception type: " + e.getClass().getName());
                System.out.println("[DEBUG] Exception message: " + e.getMessage());
                e.printStackTrace();
                return onError(exchange.getResponse(), "Unauthorized: Invalid token", HttpStatus.UNAUTHORIZED);
            }
        });
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.contains("/auth") 
            || path.equals("/emi/calculate");
    }
    
    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
    }
}
