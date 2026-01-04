package com.lms.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    private static final String FROM_EMAIL = "sravanthigurram955@gmail.com";
    private static final String FROM_NAME = "LMS Notifications";
    
    private static final String EMAIL_CSS = """
        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; text-align: center; }
        .content { padding: 20px; }
        .status { display: inline-block; padding: 8px 16px; border-radius: 5px; font-weight: bold; margin: 10px 0; background: #10B981; color: white; }
        .amount { font-size: 28px; color: #667eea; font-weight: bold; }
        .due-date { background: #FEF3C7; padding: 10px 15px; border-radius: 5px; border-left: 4px solid #F59E0B; margin: 10px 0; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
        """;

    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    public void sendLoanStatusEmail(String to, String loanNumber, String status, String messageContent) {
        String subject = "Loan Application Status - " + loanNumber;
        String html = wrapHtml("LMS Notification", "Loan Application Update", String.format("""
            <p><strong>Application:</strong> %s</p>
            <p><strong>Status:</strong> <span class='status'>%s</span></p>
            <p>%s</p>
            """, loanNumber, status, messageContent));
        sendHtmlEmail(to, subject, html);
    }

    public void sendEmiReminderEmail(String to, String loanNumber, String dueDate, String amount) {
        String subject = "EMI Payment Reminder - " + loanNumber;
        String html = wrapHtml("EMI Payment Reminder", "Payment Due", String.format("""
            <p><strong>Loan:</strong> %s</p>
            <p class='amount'>Rs. %s</p>
            <div class='due-date'><strong>Due:</strong> %s</div>
            <p>Please ensure timely payment.</p>
            """, loanNumber, amount, dueDate));
        sendHtmlEmail(to, subject, html);
    }

    private String wrapHtml(String headerTitle, String sectionTitle, String content) {
        return String.format("""
            <!DOCTYPE html><html><head><style>%s</style></head><body>
            <div class='container'>
            <div class='header'><h1>%s</h1></div>
            <div class='content'><h2>%s</h2>%s</div>
            <div class='footer'><p>Automated message from LMS</p></div>
            </div></body></html>
            """, EMAIL_CSS, headerTitle, sectionTitle, content);
    }
}
