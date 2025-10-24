package utils;

import models.User;

public class Constants {
    // Application constants
    public static final String APP_NAME = "Tailored Resume Builder";
    public static final String APP_VERSION = "1.0.0";

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
