package com.lms.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Tests")
class EmailServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SUBJECT = "Subject";
    private static final BigDecimal AMOUNT_500000 = new BigDecimal("500000");
    private static final BigDecimal AMOUNT_EMI = new BigDecimal("10324.56");

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Manually inject self-reference for @Lazy injection
        ReflectionTestUtils.setField(emailService, "self", emailService);
        // Inject the from email address
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@loaneazy.com");
    }

    @Test
    @DisplayName("Send Simple Email - success")
    void sendSimpleEmailSuccess() {
        emailService.sendSimpleEmail(TEST_EMAIL, TEST_SUBJECT, "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Send Simple Email - failure handled")
    void sendSimpleEmailFailure() {
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> 
            emailService.sendSimpleEmail(TEST_EMAIL, TEST_SUBJECT, "Body")
        );
    }

    @Test
    @DisplayName("Send HTML Email - success")
    void sendHtmlEmailSuccess() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendHtmlEmail(TEST_EMAIL, TEST_SUBJECT, "<html>Body</html>");
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    @DisplayName("Send HTML Email - exception handled")
    void sendHtmlEmailException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new org.springframework.mail.MailSendException("Send failed")).when(mailSender).send(mimeMessage);
        assertDoesNotThrow(() -> 
            emailService.sendHtmlEmail(TEST_EMAIL, TEST_SUBJECT, "<html>Body</html>")
        );
    }

    // ============ LOAN EMAILS ============

    @Test
    @DisplayName("Send Loan Applied Email")
    void sendLoanAppliedEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendLoanAppliedEmail(TEST_EMAIL, 123L, "HOME", 
            AMOUNT_500000, "2026-01-06");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Loan Approved Email")
    void sendLoanApprovedEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendLoanApprovedEmail(TEST_EMAIL, 123L, "HOME",
            AMOUNT_500000, 8.5, 60, AMOUNT_EMI, "2026-01-06");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Loan Rejected Email")
    void sendLoanRejectedEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendLoanRejectedEmail(TEST_EMAIL, 123L, "PERSONAL",
            new BigDecimal("100000"), "Low credit score");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Loan Disbursed Email")
    void sendLoanDisbursedEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendLoanDisbursedEmail(TEST_EMAIL, 123L, "HOME",
            AMOUNT_500000, AMOUNT_EMI, 60, "2026-01-07");
        verify(mailSender).send(mimeMessage);
    }

    // ============ EMI EMAILS ============

    @Test
    @DisplayName("Send EMI Due Email")
    void sendEmiDueEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendEmiDueEmail(TEST_EMAIL, 123L, "HOME", 
            1, AMOUNT_EMI, "2026-02-06", 59);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send EMI Paid Email")
    void sendEmiPaidEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendEmiPaidEmail(TEST_EMAIL, 123L, "HOME",
            1, AMOUNT_EMI, "TXN-ABC123", 59);
        verify(mailSender).send(mimeMessage);
    }

    // ============ WALLET EMAILS ============

    @Test
    @DisplayName("Send Wallet Topup Email")
    void sendWalletTopupEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendWalletTopupEmail(TEST_EMAIL, 
            new BigDecimal("5000"), new BigDecimal("15000"), "TXN-XYZ789");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Wallet Debit Email")
    void sendWalletDebitEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendWalletDebitEmail(TEST_EMAIL,
            AMOUNT_EMI, new BigDecimal("4675.44"), "EMI Payment");
        verify(mailSender).send(mimeMessage);
    }

    // ============ LEGACY EMAILS ============

    @Test
    @DisplayName("Send Loan Status Email (Legacy)")
    void sendLoanStatusEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendLoanStatusEmail(TEST_EMAIL, "LN-123", "APPROVED", "Congratulations!");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send EMI Reminder Email (Legacy)")
    void sendEmiReminderEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendEmiReminderEmail(TEST_EMAIL, "LN-123", "2026-02-01", "5000");
        verify(mailSender).send(mimeMessage);
    }
}

