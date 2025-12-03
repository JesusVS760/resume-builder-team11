package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import utils.Constants;

public class TwilioService {

    // Gmail SMTP configuration - loaded from environment variables
    private static final String SMTP_HOST = Constants.Email.SMTP_HOST;
    private static final String SMTP_PORT = Constants.Email.SMTP_PORT;
    private static final String EMAIL_USERNAME = Constants.Email.USERNAME;
    private static final String EMAIL_PASSWORD = Constants.Email.APP_PASSWORD;
    private static final String FROM_EMAIL = Constants.Email.FROM_EMAIL;
    private static final String FROM_NAME = Constants.Email.FROM_NAME;

    // In production, store codes in a database, NOT in memory
    // This is a temporary in-memory storage (codes disappear when app closes)
    private Map<String, String> verificationCodes;  // Maps email to code
    private Map<String, Long> codeTimestamps;       // Tracks when code was sent

    private static final long CODE_EXPIRATION_TIME = 10 * 60 * 1000;  // 10 minutes in milliseconds
    private static final int CODE_LENGTH = 6;  // 6-digit code

    private Session emailSession;

    // Initialize email session and storage when TwilioService is created
    public TwilioService() {
        // Check if email credentials are configured
        if (EMAIL_USERNAME == null || EMAIL_USERNAME.isEmpty() ||
            EMAIL_PASSWORD == null || EMAIL_PASSWORD.isEmpty() ||
            FROM_EMAIL == null || FROM_EMAIL.isEmpty()) {
            System.err.println("⚠️  WARNING: Email credentials not configured!");
            System.err.println("Please set EMAIL_USERNAME, EMAIL_APP_PASSWORD, and FROM_EMAIL in your .env file");
            System.err.println("Email verification will not work until credentials are configured.");
            this.emailSession = null;
        } else {
            // Configure Gmail SMTP properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Create email session with authentication
            this.emailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            System.out.println("✓ TwilioService initialized with Gmail SMTP for: " + EMAIL_USERNAME);
        }

        // Create storage for verification codes
        this.verificationCodes = new HashMap<>();
        this.codeTimestamps = new HashMap<>();
    }

    public boolean sendVerificationCode(String emailAddress) {
        try {
            // Check if email service is configured
            if (emailSession == null) {
                System.err.println("❌ Email service not configured. Please check your .env file for EMAIL_USERNAME, EMAIL_APP_PASSWORD, and FROM_EMAIL");
                return false;
            }

            // Validate email format
            if (!isValidEmail(emailAddress)) {
                System.out.println("Invalid email format: " + emailAddress);
                return false;
            }

            // Generate a 6-digit code
            String code = generateVerificationCode();

            // Create the email message
            Message message = new MimeMessage(emailSession);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
            message.setSubject("Your Resume Builder Verification Code");

            // Create HTML email body
            String htmlBody = createEmailBody(code);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            // Send the email
            System.out.println("Attempting to send email to: " + emailAddress);
            Transport.send(message);

            // If we get here, email was sent successfully
            System.out.println("Email sent successfully to: " + emailAddress);

            // Store the code and timestamp for later verification
            verificationCodes.put(emailAddress, code);
            codeTimestamps.put(emailAddress, System.currentTimeMillis());

            return true;

        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyCode(String enteredCode) {
        try {
            // Check if we have any stored codes
            if (verificationCodes.isEmpty()) {
                System.out.println("No verification codes found");
                return false;
            }

            // Check each email's code
            for (String email : verificationCodes.keySet()) {
                String storedCode = verificationCodes.get(email);
                long sentTime = codeTimestamps.get(email);
                long currentTime = System.currentTimeMillis();

                // Check if code has expired
                if (currentTime - sentTime > CODE_EXPIRATION_TIME) {
                    System.out.println("Code expired for: " + email);
                    verificationCodes.remove(email);  // Delete expired code
                    codeTimestamps.remove(email);
                    continue;  // Skip to next code
                }

                // Check if entered code matches stored code
                if (enteredCode.equals(storedCode)) {
                    System.out.println("Code verified successfully for: " + email);

                    // Clean up - remove code after verification
                    verificationCodes.remove(email);
                    codeTimestamps.remove(email);

                    return true;
                }
            }

            System.out.println("Entered code does not match any stored code");
            return false;

        } catch (Exception e) {
            System.out.println("Error verifying code: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int min = (int) Math.pow(10, CODE_LENGTH - 1);  // 100000 for 6 digits
        int max = (int) Math.pow(10, CODE_LENGTH) - min;  // 900000 for 6 digits
        int code = min + random.nextInt(max);
        return String.valueOf(code);
    }

    private boolean isValidEmail(String email) {
        // Basic email validation using regex
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private String createEmailBody(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 0; }" +
                ".header { background-color: #6F6FDE; color: white; padding: 30px 20px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; }" +
                ".content { background-color: #f9f9f9; padding: 40px 30px; }" +
                ".content h2 { color: #6F6FDE; margin-top: 0; }" +
                ".code-box { background-color: white; border: 3px solid #6F6FDE; border-radius: 8px; " +
                "padding: 20px; margin: 30px 0; text-align: center; }" +
                ".code { font-size: 36px; font-weight: bold; color: #6F6FDE; letter-spacing: 8px; " +
                "font-family: 'Courier New', monospace; }" +
                ".expiry { color: #d32f2f; font-weight: bold; margin-top: 20px; }" +
                ".footer { background-color: #6F6FDE; color: white; text-align: center; padding: 20px; " +
                "font-size: 12px; }" +
                ".note { color: #666; font-size: 14px; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Resume Builder</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Email Verification</h2>" +
                "<p>Thank you for signing up! Please use the verification code below to complete your registration:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + code + "</div>" +
                "</div>" +
                "<p class='expiry'>⏰ This code will expire in 10 minutes.</p>" +
                "<p class='note'>If you didn't request this code, please ignore this email. " +
                "Your account will remain secure.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated message from Resume Builder.</p>" +
                "<p>Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public long getTimeRemaining(String email) {
        if (!codeTimestamps.containsKey(email)) {
            return -1;  // No code exists
        }

        long sentTime = codeTimestamps.get(email);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - sentTime;
        long remaining = CODE_EXPIRATION_TIME - elapsed;

        return remaining / 1000;  // Convert milliseconds to seconds
    }

    public boolean resendVerificationCode(String email) {
        // Remove old code and timestamp
        verificationCodes.remove(email);
        codeTimestamps.remove(email);

        // Send new code
        return sendVerificationCode(email);
    }

    public void clearAllCodes() {
        verificationCodes.clear();
        codeTimestamps.clear();
        System.out.println("All verification codes cleared");
    }
}