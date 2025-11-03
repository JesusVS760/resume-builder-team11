package utils;

import models.User;

public class Constants {

    // Initialize DotEnvLoader when Constants class is loaded
    static {
        DotEnvLoader.get("DUMMY"); // This triggers the static block in DotEnvLoader
    }
    // Application constants
    public static final String APP_NAME = "Tailored Resume Builder";
    public static final String APP_VERSION = "1.0.0";

    // OAuth Configuration - LOADED FROM ENVIRONMENT VARIABLES
    public static final class OAuth {
        // Load credentials from .env file or environment variables
        // This ensures no sensitive data is hardcoded in source code
        public static final String GOOGLE_CLIENT_ID = DotEnvLoader.get("GOOGLE_CLIENT_ID", "your-google-oauth-client-id");
        public static final String GOOGLE_CLIENT_SECRET = DotEnvLoader.get("GOOGLE_CLIENT_SECRET", "your-google-oauth-client-secret");
        public static final String GITHUB_CLIENT_ID = DotEnvLoader.get("GITHUB_CLIENT_ID", "your-github-oauth-client-id");
        public static final String GITHUB_CLIENT_SECRET = DotEnvLoader.get("GITHUB_CLIENT_SECRET", "your-github-oauth-client-secret");
    }

    // Email Configuration - LOADED FROM ENVIRONMENT VARIABLES
    public static final class Email {
        // Gmail SMTP configuration for email verification
        public static final String SMTP_HOST = "smtp.gmail.com";
        public static final String SMTP_PORT = "587";
        public static final String USERNAME = DotEnvLoader.get("EMAIL_USERNAME", "your-email@gmail.com");
        public static final String APP_PASSWORD = DotEnvLoader.get("EMAIL_APP_PASSWORD", "your-app-password");
        public static final String FROM_EMAIL = DotEnvLoader.get("FROM_EMAIL", "your-email@gmail.com");
        public static final String FROM_NAME = "Resume Builder";
    }

    // Session management
    public static class Session {
        private static User currentUser = null;
        private static boolean justLoggedIn = false;

        public static void login(User user) {
            currentUser = user;
            justLoggedIn = true;
        }

        public static void logout() {
            currentUser = null;
            justLoggedIn = false;
        }

        public static User getCurrentUser() {
            return currentUser;
        }

        public static boolean isLoggedIn() {
            return currentUser != null;
        }

        public static String getCurrentUserEmail() {
            return currentUser != null ? currentUser.getEmail() : null;
        }

        public static String getCurrentUserName() {
            return currentUser != null ? currentUser.getName() : null;
        }

        public static boolean justLoggedIn() {
            boolean result = justLoggedIn;
            justLoggedIn = false; // Reset the flag
            return result;
        }
    }
}
