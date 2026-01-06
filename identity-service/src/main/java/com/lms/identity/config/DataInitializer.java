package com.lms.identity.config;

import com.lms.identity.entity.User;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the ROOT_ADMIN user on startup.
 * Only ROOT_ADMIN is seeded since:
 * - Customers can self-register
 * - Admins/Officers are created by ROOT_ADMIN
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.root-admin.email:root@lms.com}")
    private String rootAdminEmail;

    @Value("${app.seed.root-admin.password:RootAdmin@123}")
    private String rootAdminPassword;

    @Override
    public void run(String... args) {
        log.info("Checking for ROOT_ADMIN seed...");
        
        if (!userRepository.existsByEmail(rootAdminEmail)) {
            User rootAdmin = User.builder()
                    .email(rootAdminEmail)
                    .passwordHash(passwordEncoder.encode(rootAdminPassword))
                    .firstName("Root")
                    .lastName("Admin")
                    .phone("+919999999999")
                    .role(User.Role.ROOT_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(rootAdmin);
            log.info("ROOT_ADMIN created: {}", rootAdminEmail);
        } else {
            log.info("ROOT_ADMIN already exists: {}", rootAdminEmail);
        }
    }
}
