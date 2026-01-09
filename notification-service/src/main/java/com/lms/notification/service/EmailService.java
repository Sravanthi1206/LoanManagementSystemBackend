package com.lms.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailService self;
    private final String fromEmail;
    
    private static final String FROM_NAME = "LoanEazy";
    private static final String BRAND_COLOR = "#1a1a2e";
    private static final String SUCCESS_COLOR = "#10B981";
    private static final String WARNING_COLOR = "#F59E0B";
    private static final String ERROR_COLOR = "#EF4444";
    
    private static final String THEME_SUCCESS = "success";
    private static final String THEME_WARNING = "warning";
    private static final String THEME_INFO = "info";

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));

    public EmailService(JavaMailSender mailSender, 
                        @org.springframework.context.annotation.Lazy EmailService self,
                        @org.springframework.beans.factory.annotation.Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.self = self;
        this.fromEmail = fromEmail;
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
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
            helper.setFrom(fromEmail, FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    // ============ LOAN APPLICATION EMAILS ============

    public void sendLoanAppliedEmail(String to, Long loanId, String loanType, BigDecimal amount, String appliedDate) {
        String subject = "Loan Application Received - #" + loanId;
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>Thank you for choosing <strong>LoanEazy</strong>! Your loan application has been successfully submitted.</p>
            
            <div class='info-box'>
                <table class='details-table'>
                    <tr><td>Application ID</td><td><strong>#%d</strong></td></tr>
                    <tr><td>Loan Type</td><td>%s</td></tr>
                    <tr><td>Amount Requested</td><td class='amount'>%s</td></tr>
                    <tr><td>Applied On</td><td>%s</td></tr>
                </table>
            </div>
            
            <p><strong>What's Next?</strong></p>
            <ul>
                <li>Our loan officer will review your application</li>
                <li>You may be contacted for additional documents</li>
                <li>Track status in your LoanEazy dashboard</li>
            </ul>
            """, loanId, loanType, formatCurrency(amount), appliedDate);
        self.sendHtmlEmail(to, subject, wrapHtml("Application Received", content, THEME_INFO));
    }

    @SuppressWarnings("java:S107") // Suppress too many parameters warning
    public void sendLoanApprovedEmail(String to, Long loanId, String loanType, BigDecimal approvedAmount, double interestRate, 
                                       int tenure, BigDecimal emi, String approvalDate) {
        String subject = "🎉 Congratulations! Loan Approved - #" + loanId;
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>Great news! Your loan application has been <span class='status-success'>APPROVED</span>!</p>
            
            <div class='info-box success-box'>
                <h3>Loan Details</h3>
                <table class='details-table'>
                    <tr><td>Loan ID</td><td><strong>#%d</strong></td></tr>
                    <tr><td>Loan Type</td><td>%s</td></tr>
                    <tr><td>Approved Amount</td><td class='amount'>%s</td></tr>
                    <tr><td>Interest Rate</td><td>%.2f%% p.a.</td></tr>
                    <tr><td>Tenure</td><td>%d months</td></tr>
                    <tr><td>Monthly EMI</td><td class='amount'>%s</td></tr>
                    <tr><td>Approval Date</td><td>%s</td></tr>
                </table>
            </div>
            
            <p>The loan amount will be disbursed to your registered wallet shortly.</p>
            """, loanId, loanType, formatCurrency(approvedAmount), interestRate, tenure, formatCurrency(emi), approvalDate);
        self.sendHtmlEmail(to, subject, wrapHtml("Loan Approved!", content, THEME_SUCCESS));
    }

    public void sendLoanRejectedEmail(String to, Long loanId, String loanType, BigDecimal requestedAmount, String reason) {
        String subject = "Loan Application Update - #" + loanId;
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>We regret to inform you that your loan application has not been approved at this time.</p>
            
            <div class='info-box warning-box'>
                <table class='details-table'>
                    <tr><td>Application ID</td><td><strong>#%d</strong></td></tr>
                    <tr><td>Loan Type</td><td>%s</td></tr>
                    <tr><td>Amount Requested</td><td class='amount'>%s</td></tr>
                    <tr><td>Status</td><td><span class='status-error'>Not Approved</span></td></tr>
                    <tr><td>Reason</td><td>%s</td></tr>
                </table>
            </div>
            
            <p>You may apply again after addressing the above concerns. Contact our support team for assistance.</p>
            """, loanId, loanType, formatCurrency(requestedAmount), reason);
        self.sendHtmlEmail(to, subject, wrapHtml("Application Update", content, THEME_WARNING));
    }

    public void sendLoanDisbursedEmail(String to, Long loanId, String loanType, BigDecimal amount, BigDecimal emi, int tenure, String disbursementDate) {
        String subject = "💰 Loan Disbursed - #" + loanId;
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>Your loan has been <span class='status-success'>DISBURSED</span> to your LoanEazy wallet!</p>
            
            <div class='info-box success-box'>
                <table class='details-table'>
                    <tr><td>Loan ID</td><td><strong>#%d</strong></td></tr>
                    <tr><td>Loan Type</td><td>%s</td></tr>
                    <tr><td>Disbursed Amount</td><td class='amount'>%s</td></tr>
                    <tr><td>Monthly EMI</td><td class='amount'>%s</td></tr>
                    <tr><td>Tenure</td><td>%d months</td></tr>
                    <tr><td>Disbursement Date</td><td>%s</td></tr>
                    <tr><td>Credited To</td><td>Your LoanEazy Wallet</td></tr>
                </table>
            </div>
            
            <p>You can view your wallet balance in the dashboard. EMI payments will start from next month.</p>
            """, loanId, loanType, formatCurrency(amount), formatCurrency(emi), tenure, disbursementDate);
        self.sendHtmlEmail(to, subject, wrapHtml("Loan Disbursed", content, THEME_SUCCESS));
    }

    // ============ EMI EMAILS ============

    public void sendEmiDueEmail(String to, Long loanId, String loanType, int installmentNumber, BigDecimal amount, String dueDate, int remainingInstallments) {
        String subject = "⏰ EMI Payment Reminder - Loan #" + loanId;
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>This is a friendly reminder that your EMI payment is due soon.</p>
            
            <div class='info-box warning-box'>
                <table class='details-table'>
                    <tr><td>Loan ID</td><td><strong>#%d</strong></td></tr>
                    <tr><td>Loan Type</td><td>%s</td></tr>
                    <tr><td>Installment</td><td>#%d</td></tr>
                    <tr><td>Amount Due</td><td class='amount'>%s</td></tr>
                    <tr><td>Due Date</td><td><strong>%s</strong></td></tr>
                    <tr><td>Remaining EMIs</td><td>%d</td></tr>
                </table>
            </div>
            
            <p><strong>Payment Options:</strong></p>
            <ul>
                <li>Pay via Wallet balance in your dashboard</li>
                <li>Top-up wallet using Card/UPI and pay</li>
            </ul>
            <p>Avoid late payment charges by paying on time.</p>
            """, loanId, loanType, installmentNumber, formatCurrency(amount), dueDate, remainingInstallments);
        self.sendHtmlEmail(to, subject, wrapHtml("EMI Reminder", content, THEME_WARNING));
    }

    public void sendEmiPaidEmail(String to, Long loanId, String loanType, int installmentNumber, BigDecimal amount, String transactionId, int remainingInstallments) {
        String subject = "✅ EMI Payment Confirmed - Loan #" + loanId;
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>Your EMI payment has been <span class='status-success'>successfully received</span>!</p>
            
            <div class='info-box success-box'>
                <table class='details-table'>
                    <tr><td>Loan ID</td><td><strong>#%d</strong></td></tr>
                    <tr><td>Loan Type</td><td>%s</td></tr>
                    <tr><td>Installment Paid</td><td>#%d</td></tr>
                    <tr><td>Amount Paid</td><td class='amount'>%s</td></tr>
                    <tr><td>Transaction ID</td><td><code>%s</code></td></tr>
                    <tr><td>Remaining EMIs</td><td>%d</td></tr>
                </table>
            </div>
            
            <p>Thank you for your timely payment. View your payment history in the dashboard.</p>
            """, loanId, loanType, installmentNumber, formatCurrency(amount), transactionId, remainingInstallments);
        self.sendHtmlEmail(to, subject, wrapHtml("Payment Confirmed", content, THEME_SUCCESS));
    }

    // ============ WALLET EMAILS ============

    public void sendWalletTopupEmail(String to, BigDecimal amount, BigDecimal newBalance, String transactionId) {
        String subject = "💳 Wallet Top-up Successful";
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>Your wallet has been <span class='status-success'>topped up</span> successfully!</p>
            
            <div class='info-box success-box'>
                <table class='details-table'>
                    <tr><td>Amount Added</td><td class='amount'>%s</td></tr>
                    <tr><td>New Balance</td><td class='amount'>%s</td></tr>
                    <tr><td>Transaction ID</td><td><code>%s</code></td></tr>
                </table>
            </div>
            
            <p>You can use this balance to pay your EMIs.</p>
            """, formatCurrency(amount), formatCurrency(newBalance), transactionId);
        self.sendHtmlEmail(to, subject, wrapHtml("Wallet Top-up", content, THEME_SUCCESS));
    }

    public void sendWalletDebitEmail(String to, BigDecimal amount, BigDecimal newBalance, String purpose) {
        String subject = "📤 Wallet Debited";
        String content = String.format("""
            <p>Dear Customer,</p>
            <p>An amount has been debited from your wallet.</p>
            
            <div class='info-box'>
                <table class='details-table'>
                    <tr><td>Amount Debited</td><td class='amount'>%s</td></tr>
                    <tr><td>Purpose</td><td>%s</td></tr>
                    <tr><td>Remaining Balance</td><td class='amount'>%s</td></tr>
                </table>
            </div>
            """, formatCurrency(amount), purpose, formatCurrency(newBalance));
        self.sendHtmlEmail(to, subject, wrapHtml("Wallet Transaction", content, THEME_INFO));
    }

    // ============ ACCOUNT EMAILS ============

    public void sendAccountCredentialsEmail(String to, String firstName, String role, String email, String password) {
        String subject = "Your LMS Account Has Been Created";
        String roleDisplay = role.replace("_", " ");
        String content = String.format("""
            <p>Dear %s,</p>
            <p>Your <strong>%s</strong> account has been created in the Loan Management System.</p>
            
            <div class='info-box success-box'>
                <h3>Login Credentials</h3>
                <table class='details-table'>
                    <tr><td>Email</td><td><strong>%s</strong></td></tr>
                    <tr><td>Temporary Password</td><td><code>%s</code></td></tr>
                    <tr><td>Role</td><td>%s</td></tr>
                </table>
            </div>
            
            <p><strong>⚠️ Important:</strong> Please change your password after first login for security.</p>
            
            <p><strong>Getting Started:</strong></p>
            <ul>
                <li>Login to the LMS portal using the credentials above</li>
                <li>Update your temporary password immediately</li>
                <li>Complete your profile information</li>
            </ul>
            """, firstName, roleDisplay, email, password, roleDisplay);
        self.sendHtmlEmail(to, subject, wrapHtml("Account Created", content, THEME_SUCCESS));
    }

    public void sendAccountActivatedEmail(String to, String firstName, String role) {
        String subject = "✅ Your LMS Account Has Been Activated";
        String roleDisplay = role.replace("_", " ");
        String content = String.format("""
            <p>Dear %s,</p>
            <p>Great news! Your <strong>%s</strong> account in the Loan Management System has been <span class='status-success'>ACTIVATED</span>.</p>
            
            <div class='info-box success-box'>
                <p>You can now log in and access all your account features.</p>
            </div>
            
            <p><strong>What you can do now:</strong></p>
            <ul>
                <li>Login to the LMS portal</li>
                <li>Access your dashboard</li>
                <li>Perform your role-specific tasks</li>
            </ul>
            """, firstName, roleDisplay);
        self.sendHtmlEmail(to, subject, wrapHtml("Account Activated", content, THEME_SUCCESS));
    }

    public void sendAccountDeactivatedEmail(String to, String firstName, String role) {
        String subject = "⚠️ Your LMS Account Has Been Deactivated";
        String roleDisplay = role.replace("_", " ");
        String content = String.format("""
            <p>Dear %s,</p>
            <p>Your <strong>%s</strong> account in the Loan Management System has been <span class='status-error'>DEACTIVATED</span>.</p>
            
            <div class='info-box warning-box'>
                <p>You will no longer be able to log in or access your account features.</p>
            </div>
            
            <p>If you believe this was done in error or have any questions, please contact your administrator or support team.</p>
            """, firstName, roleDisplay);
        self.sendHtmlEmail(to, subject, wrapHtml("Account Deactivated", content, THEME_WARNING));
    }

    // ============ LEGACY METHODS ============

    public void sendLoanStatusEmail(String to, String loanNumber, String status, String messageContent) {
        String subject = "Loan Application Status - " + loanNumber;
        String content = String.format("""
            <p><strong>Application:</strong> %s</p>
            <p><strong>Status:</strong> <span class='status-info'>%s</span></p>
            <p>%s</p>
            """, loanNumber, status, messageContent);
        self.sendHtmlEmail(to, subject, wrapHtml("Loan Update", content, THEME_INFO));
    }

    public void sendEmiReminderEmail(String to, String loanNumber, String dueDate, String amount) {
        String subject = "EMI Payment Reminder - " + loanNumber;
        String content = String.format("""
            <p><strong>Loan:</strong> %s</p>
            <p class='amount'>Rs. %s</p>
            <div class='due-date'><strong>Due:</strong> %s</div>
            <p>Please ensure timely payment.</p>
            """, loanNumber, amount, dueDate);
        self.sendHtmlEmail(to, subject, wrapHtml("EMI Reminder", content, THEME_WARNING));
    }

    // ============ HELPERS ============

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return CURRENCY.format(amount);
    }

    private String wrapHtml(String title, String content, String theme) {
        String themeColor = switch (theme) {
            case THEME_SUCCESS -> SUCCESS_COLOR;
            case THEME_WARNING -> WARNING_COLOR;
            case "error" -> ERROR_COLOR;
            default -> BRAND_COLOR;
        };
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                    .header { background: %s; color: white; padding: 25px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; }
                    .logo { font-size: 28px; font-weight: bold; margin-bottom: 5px; }
                    .content { padding: 30px; line-height: 1.6; color: #333; }
                    .info-box { background: #f8f9fa; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .success-box { background: #ecfdf5; border-left: 4px solid %s; }
                    .warning-box { background: #fffbeb; border-left: 4px solid %s; }
                    .details-table { width: 100%%; border-collapse: collapse; }
                    .details-table td { padding: 10px 0; border-bottom: 1px solid #eee; }
                    .details-table td:first-child { color: #666; width: 40%%; }
                    .amount { color: %s; font-weight: bold; font-size: 1.1em; }
                    .status-success { background: %s; color: white; padding: 3px 10px; border-radius: 4px; }
                    .status-error { background: %s; color: white; padding: 3px 10px; border-radius: 4px; }
                    .status-info { background: %s; color: white; padding: 3px 10px; border-radius: 4px; }
                    code { background: #f0f0f0; padding: 2px 6px; border-radius: 4px; font-family: monospace; }
                    ul { padding-left: 20px; }
                    li { margin: 8px 0; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">LoanEazy</div>
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        <p>This is an automated message from LoanEazy. Please do not reply.</p>
                        <p>© 2026 LoanEazy. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, BRAND_COLOR, SUCCESS_COLOR, WARNING_COLOR, themeColor,
                SUCCESS_COLOR, ERROR_COLOR, BRAND_COLOR, title, content);
    }
}
