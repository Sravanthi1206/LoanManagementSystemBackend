package com.lms.identity.config;

import com.lms.identity.entity.User;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes test data for the Identity Service.
 * Creates admin, officer, and customer accounts on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking for seed data...");
        
        // Create Admin user
        if (!userRepository.existsByEmail("admin@lms.com")) {
            User admin = User.builder()
                    .email("admin@lms.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .firstName("Test")
                    .lastName("Admin")
                    .phone("+919876543212")
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Created Admin user: admin@lms.com / Password@123");
        }

        // Create Loan Officer user
        if (!userRepository.existsByEmail("officer@lms.com")) {
            User officer = User.builder()
                    .email("officer@lms.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .firstName("Test")
                    .lastName("Officer")
                    .phone("+919876543211")
                    .role(User.Role.LOAN_OFFICER)
                    .active(true)
                    .build();
            userRepository.save(officer);
            log.info("✅ Created Loan Officer user: officer@lms.com / Password@123");
        }

        // Create Customer user
        if (!userRepository.existsByEmail("customer@lms.com")) {
            User customer = User.builder()
                    .email("customer@lms.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .firstName("Test")
                    .lastName("Customer")
                    .phone("+919876543210")
                    .role(User.Role.CUSTOMER)
                    .active(true)
                    .build();
            userRepository.save(customer);
            log.info("✅ Created Customer user: customer@lms.com / Password@123");
        }

        log.info("===== TEST CREDENTIALS =====");
        log.info("Admin:    admin@lms.com    / Password@123");
        log.info("Officer:  officer@lms.com  / Password@123");
        log.info("Customer: customer@lms.com / Password@123");
        log.info("============================");
    }
}
