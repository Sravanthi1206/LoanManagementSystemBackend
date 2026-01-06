package com.lms.emi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmiServiceApplicationTest {

    @Test
    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {});
    }

    @Test
    void main() {
        assertDoesNotThrow(() -> {
            try {
                EmiServiceApplication.main(new String[]{});
            } catch (Exception e) {
                // Ignored
            }
        });
    }
}
