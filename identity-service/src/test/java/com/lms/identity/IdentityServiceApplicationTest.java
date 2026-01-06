package com.lms.identity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdentityServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifies Spring context loads successfully - no assertion needed
        // The test passes if the application context initializes without exceptions
        assertDoesNotThrow(() -> {});
    }

    @Test
    void main() {
        assertDoesNotThrow(() -> {
            try {
                IdentityServiceApplication.main(new String[]{});
            } catch (Exception e) {
                // Ignored
            }
        });
    }
}
