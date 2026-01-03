package com.lms.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceApplicationTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.lms.notification.repository.NotificationRepository notificationRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        // Verify context loads - this covers the Application class
    }
}
