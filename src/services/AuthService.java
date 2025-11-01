package services;

import models.User;
import dao.UserDAO;
import utils.PasswordUtil;

public class AuthService {
    private UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    public boolean signup(String email, String password, String name) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Validate email format
        if (!isValidEmail(email.trim())) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Name is optional - use email prefix if not provided
        if (name == null || name.trim().isEmpty()) {
            name = email.split("@")[0]; // Use part before @ as default name
        }
        
        // Check if email already exists
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Validate password strength
        if (!PasswordUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters with at least one letter and one number");
        }
        
        // Create user object
        User user = new User(email.trim().toLowerCase(), PasswordUtil.hashPassword(password), name.trim());
        
        // Save to database
        return userDAO.saveUser(user);
    }

    private boolean isValidEmail(String email) {
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    public User login(String email, String password) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Validate email format
        if (!isValidEmail(email.trim())) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Check for admin login
        if (email.trim().equalsIgnoreCase("Admin") && password.equals("123")) {
            User adminUser = new User();
            adminUser.setEmail("Admin");
            adminUser.setName("Administrator");
            adminUser.setId("ADMIN"); // Special ID for admin
            return adminUser;
        }
        
        // Find user by email
        User user = userDAO.findByEmail(email.trim().toLowerCase());
        
        if (user == null) {
            return null; // User not found
        }
        
        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return null; // Invalid password
        }

        return user;
    }
    
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        // Validate input
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password cannot be empty");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        
        // Get user
        User user = userDAO.findByEmail(""); // This needs to be fixed - we need a method to get user by ID
        
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Verify current password
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password strength
        if (!PasswordUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 6 characters with at least one letter and one number");
        }
        
        // Update password
        String newPasswordHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newPasswordHash);
    }
    
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userDAO.emailExists(email.trim().toLowerCase());
    }

    // Direct OAuth Methods
    public User googleLogin() throws Exception {
        OAuthService oauthService = new OAuthService();
        User user = oauthService.signInWithGoogle();

        if (user != null) {
            // Store user session
            utils.Constants.Session.login(user);
            return user;
        }

        throw new Exception("Google OAuth authentication failed");
    }

    // Unified OAuth methods - handles both login and signup automatically
    public User continueWithGoogle() throws Exception {
        OAuthService oauthService = new OAuthService();
        User user = oauthService.continueWithGoogle();

        if (user != null) {
            // Store user session
            utils.Constants.Session.login(user);
            return user;
        }

        throw new Exception("Google OAuth authentication failed");
    }

    public User continueWithGitHub() throws Exception {
        OAuthService oauthService = new OAuthService();
        User user = oauthService.continueWithGitHub();

        if (user != null) {
            // Store user session
            utils.Constants.Session.login(user);
            return user;
        }

        throw new Exception("GitHub OAuth authentication failed");
    }
}
