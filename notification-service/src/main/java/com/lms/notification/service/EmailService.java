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

    /**
     * Send a simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send HTML formatted email
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            
            mailSender.send(message);
            log.info("HTML Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send loan status notification email
     */
    public void sendLoanStatusEmail(String to, String loanNumber, String status, String message) {
        String subject = "Loan Application Status Update - " + loanNumber;
        String htmlBody = buildLoanStatusEmailHtml(loanNumber, status, message);
        sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send EMI reminder email
     */
    public void sendEmiReminderEmail(String to, String loanNumber, String dueDate, String amount) {
        String subject = "EMI Payment Reminder - " + loanNumber;
        String htmlBody = buildEmiReminderEmailHtml(loanNumber, dueDate, amount);
        sendHtmlEmail(to, subject, htmlBody);
    }

    private String buildLoanStatusEmailHtml(String loanNumber, String status, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>");
        sb.append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }");
        sb.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; text-align: center; }");
        sb.append(".content { padding: 20px; }");
        sb.append(".status { display: inline-block; padding: 8px 16px; border-radius: 5px; font-weight: bold; margin: 10px 0; background: #10B981; color: white; }");
        sb.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        sb.append("</style></head><body>");
        sb.append("<div class='container'>");
        sb.append("<div class='header'><h1>LMS Notification</h1></div>");
        sb.append("<div class='content'>");
        sb.append("<h2>Loan Application Update</h2>");
        sb.append("<p><strong>Application Number:</strong> ").append(loanNumber).append("</p>");
        sb.append("<p><strong>Status:</strong> <span class='status'>").append(status).append("</span></p>");
        sb.append("<p>").append(message).append("</p>");
        sb.append("</div>");
        sb.append("<div class='footer'><p>This is an automated message from Loan Management System.</p>");
        sb.append("<p>2026 LMS - All rights reserved</p></div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildEmiReminderEmailHtml(String loanNumber, String dueDate, String amount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>");
        sb.append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }");
        sb.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; text-align: center; }");
        sb.append(".content { padding: 20px; }");
        sb.append(".amount { font-size: 28px; color: #667eea; font-weight: bold; }");
        sb.append(".due-date { background: #FEF3C7; padding: 10px 15px; border-radius: 5px; border-left: 4px solid #F59E0B; margin: 10px 0; }");
        sb.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        sb.append("</style></head><body>");
        sb.append("<div class='container'>");
        sb.append("<div class='header'><h1>EMI Payment Reminder</h1></div>");
        sb.append("<div class='content'>");
        sb.append("<h2>Payment Due</h2>");
        sb.append("<p><strong>Loan Number:</strong> ").append(loanNumber).append("</p>");
        sb.append("<p class='amount'>Rs. ").append(amount).append("</p>");
        sb.append("<div class='due-date'><p><strong>Due Date:</strong> ").append(dueDate).append("</p></div>");
        sb.append("<p style='margin-top: 20px;'>Please ensure timely payment to avoid late fees.</p>");
        sb.append("</div>");
        sb.append("<div class='footer'><p>This is an automated reminder from Loan Management System.</p>");
        sb.append("<p>2026 LMS - All rights reserved</p></div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }
}
