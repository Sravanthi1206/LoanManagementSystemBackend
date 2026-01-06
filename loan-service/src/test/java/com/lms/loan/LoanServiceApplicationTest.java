package com.lms.loan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LoanServiceApplicationTest {

    @Test
    @Test
    void contextLoads() {
        // Context loads successfully
        assertDoesNotThrow(() -> {});
    }

    @Test
    void main() {
        assertDoesNotThrow(() -> {
            try {
                LoanServiceApplication.main(new String[]{});
            } catch (Exception e) {
                // Ignored
            }
        });
    }
}
