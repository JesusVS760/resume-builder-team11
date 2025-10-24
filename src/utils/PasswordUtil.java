package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {
    
    public static String hashPassword(String password) {
        try {
            // Create SHA-256 hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Add salt for extra security
            String salt = generateSalt();
            String saltedPassword = password + salt;
            
            // Hash the password
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString() + ":" + salt;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            // Split hash and salt
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            String hash = parts[0];
            String salt = parts[1];
            
            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            String newHash = sb.toString();
            
            // Compare hashes
            return hash.equals(newHash);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error verifying password", e);
        }
    }
    
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public static boolean isValidPassword(String password) {
        // Basic password validation
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Check for at least one letter and one number
        boolean hasLetter = false;
        boolean hasNumber = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }
        
        return hasLetter && hasNumber;
    }
}
