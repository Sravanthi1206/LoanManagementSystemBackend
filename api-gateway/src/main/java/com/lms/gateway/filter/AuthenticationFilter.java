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

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            
            log.debug("Processing request for path: {}", path);
            
            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Skipping auth for public endpoint: {}", path);
                return chain.filter(exchange);
            }

            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for: {}", path);
                return onError(exchange.getResponse(), HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            log.debug("Auth header present, length: {}", (authHeader != null ? authHeader.length() : 0));
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
                log.debug("Token extracted, length: {}", authHeader.length());
            } else {
                log.warn("Invalid auth header format");
                return onError(exchange.getResponse(), HttpStatus.UNAUTHORIZED);
            }

            try {
                log.debug("Attempting to validate token...");
                jwtUtil.validateToken(authHeader);
                log.debug("Token validation successful!");
                
                // Extract email, role, and userId from token and add them as headers for downstream services
                String email = jwtUtil.getEmailFromToken(authHeader);
                String role = jwtUtil.getRoleFromToken(authHeader);
                Long userId = jwtUtil.getUserIdFromToken(authHeader);
                
                log.debug("Extracted claims - email: {}, role: {}, userId: {}", email, role, userId);
                
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                        .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("Token validation FAILED! Exception: {}, Message: {}", e.getClass().getName(), e.getMessage());
                e.printStackTrace();
                return onError(exchange.getResponse(), HttpStatus.UNAUTHORIZED);
            }
        });
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.contains("/auth") 
            || path.equals("/emi/calculate");
    }
    
    private Mono<Void> onError(ServerHttpResponse response, HttpStatus status) {
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        // Configuration placeholder for filter customization
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
