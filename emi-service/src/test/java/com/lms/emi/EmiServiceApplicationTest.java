package com.lms.emi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmiServiceApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main() {
        try {
            EmiServiceApplication.main(new String[]{});
        } catch (Exception e) {
            // Ignored
        }
    }
}
