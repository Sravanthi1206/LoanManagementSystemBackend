package com.lms.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdentityServiceApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main() {
        try {
            IdentityServiceApplication.main(new String[]{});
        } catch (Exception e) {
            // Ignored
        }
    }
}
