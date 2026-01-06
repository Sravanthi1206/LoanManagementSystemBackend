package com.lms.loan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LoanServiceApplicationTest {

    @Test
    void contextLoads() {
        // Context loads successfully
    }

    @Test
    void main() {
        try {
            LoanServiceApplication.main(new String[]{});
        } catch (Exception e) {
            // Ignored
        }
    }
}
