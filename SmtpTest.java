import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class SmtpTest {
    public static void main(String[] args) {
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "sravanthigurram955@gmail.com";
        String password = "blmvcdbtosuvhlzt";
        String to = "sravanthigurram955@gmail.com";
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        
        System.out.println("Testing SMTP connection to " + host + ":" + port);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password.substring(0, 4) + "****");
        
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "LMS Test"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("LMS SMTP Test - " + System.currentTimeMillis());
            message.setText("This email confirms SMTP is working correctly!");
            
            Transport.send(message);
            System.out.println("\n✅ SUCCESS! Email sent to " + to);
            
        } catch (AuthenticationFailedException e) {
            System.out.println("\n❌ AUTHENTICATION FAILED!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("\nPossible causes:");
            System.out.println("1. App Password is incorrect or expired");
            System.out.println("2. 2-Step Verification is not enabled on the Gmail account");
            System.out.println("3. App Password was revoked");
        } catch (Exception e) {
            System.out.println("\n❌ FAILED: " + e.getClass().getSimpleName());
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
