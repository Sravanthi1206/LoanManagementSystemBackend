package com.lms.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Send Simple Email - success")
    void sendSimpleEmail_Success() {
        emailService.sendSimpleEmail("test@example.com", "Subject", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Send Simple Email - failure handled")
    void sendSimpleEmail_Failure() {
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> 
            emailService.sendSimpleEmail("test@example.com", "Subject", "Body")
        );
    }

    @Test
    @DisplayName("Send HTML Email - success")
    void sendHtmlEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendHtmlEmail("test@example.com", "Subject", "<html>Body</html>");

        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Send HTML Email - messaging exception handled")
    void sendHtmlEmail_MessagingException() {
        // Need to simulate MimeMessageHelper failure or send failure
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new org.springframework.mail.MailSendException("Send failed")).when(mailSender).send(mimeMessage); // MailSendException is Runtime

        assertDoesNotThrow(() -> 
            emailService.sendHtmlEmail("test@example.com", "Subject", "<html>Body</html>")
        );
    }

    @Test
    @DisplayName("Send Loan Status Email")
    void sendLoanStatusEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendLoanStatusEmail("test@example.com", "LN-123", "APPROVED", "Congratulations!");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send EMI Reminder Email")
    void sendEmiReminderEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmiReminderEmail("test@example.com", "LN-123", "2026-02-01", "5000");

        verify(mailSender).send(mimeMessage);
    }
}
