package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class TwilioService {

    private static final String ACCOUNT_SID = System.getProperty("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getProperty("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_PHONE = System.getProperty("TWILIO_PHONE_NUMBER");

    // In production, store codes in a database, NOT in memory
    // This is a temporary in-memory storage (codes disappear when app closes)
    private Map<String, String> verificationCodes;  // Maps phone number to code
    private Map<String, Long> codeTimestamps;       // Tracks when code was sent

    private static final long CODE_EXPIRATION_TIME = 10 * 60 * 1000;  // 10 minutes in milliseconds
    private static final int CODE_LENGTH = 6;  // 6-digit code

    // Initialize Twilio and storage when TwilioService is created
    public TwilioService() {
        // Initialize Twilio with your credentials
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // Create storage for verification codes
        this.verificationCodes = new HashMap<>();
        this.codeTimestamps = new HashMap<>();
    }


    public boolean sendVerificationCode(String phoneNumber) {
        try {
            // Validate phone number format
            if (!isValidPhoneNumber(phoneNumber)) {
                System.out.println("Invalid phone number format: " + phoneNumber);
                return false;
            }

            // Generate a 6-digit code
            String code = generateVerificationCode();

            // create the SMS message
            String messageBody = "Your Resume Builder verification code is: " + code +
                    "\nThis code expires in 10 minutes.";

            // send the SMS using Twilio
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(TWILIO_PHONE),      // From your Twilio number
                    messageBody                          // Message text
            ).create();

            // If we get here, message was sent successfully
            System.out.println("Message sent with SID: " + message.getSid());

            // Store the code and timestamp for later verification
            verificationCodes.put(phoneNumber, code);
            codeTimestamps.put(phoneNumber, System.currentTimeMillis());

            return true;

        } catch (Exception e) {
            // Log the error
            System.out.println("Error sending SMS: " + e.getMessage());
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

            // Check each phone number's code
            for (String phoneNumber : verificationCodes.keySet()) {
                String storedCode = verificationCodes.get(phoneNumber);
                long sentTime = codeTimestamps.get(phoneNumber);
                long currentTime = System.currentTimeMillis();

                // Check if code has expired
                if (currentTime - sentTime > CODE_EXPIRATION_TIME) {
                    System.out.println("Code expired for: " + phoneNumber);
                    verificationCodes.remove(phoneNumber);  // Delete expired code
                    codeTimestamps.remove(phoneNumber);
                    continue;  // Skip to next code
                }

                // Check if entered code matches stored code
                if (enteredCode.equals(storedCode)) {
                    System.out.println("Code verified successfully for: " + phoneNumber);

                    // Clean up - remove code after verification
                    verificationCodes.remove(phoneNumber);
                    codeTimestamps.remove(phoneNumber);

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
        int code = 100000 + random.nextInt(900000);  // Generate number between 100000-999999
        return String.valueOf(code);
    }


    private boolean isValidPhoneNumber(String phoneNumber) {
        // Check if phone number matches format: +1234567890
        return phoneNumber != null && phoneNumber.matches("^\\+[1-9]\\d{1,14}$");
    }


    public long getTimeRemaining(String phoneNumber) {
        if (!codeTimestamps.containsKey(phoneNumber)) {
            return -1;  // No code exists
        }

        long sentTime = codeTimestamps.get(phoneNumber);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - sentTime;
        long remaining = CODE_EXPIRATION_TIME - elapsed;

        return remaining / 1000;  // Convert milliseconds to seconds
    }

    public boolean resendVerificationCode(String phoneNumber) {
        // Remove old code and timestamp
        verificationCodes.remove(phoneNumber);
        codeTimestamps.remove(phoneNumber);

        // Send new code
        return sendVerificationCode(phoneNumber);
    }


    public void clearAllCodes() {
        verificationCodes.clear();
        codeTimestamps.clear();
        System.out.println("All verification codes cleared");
    }
}