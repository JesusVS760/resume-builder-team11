package controllers;

import models.User;
import services.AuthService;
import ui.LoginFrame;

import javax.swing.SwingWorker;

public class LoginController extends BaseController<LoginFrame> {
    private final AuthService authService;  // Auth service for oauth
    private final Runnable onSuccess;       // Callback for when login works
    private final Runnable onGoToSignup;    // Callback to go from login to signup

    public LoginController(LoginFrame view,
                           AuthService authService,
                           Runnable onSuccess,
                           Runnable onGoToSignup) {
        super(view);
        this.authService = authService;
        this.onSuccess = onSuccess;
        this.onGoToSignup = onGoToSignup;
        attach();   // Wire the button actions
    }

    // Attach event listeners to the login view
    private void attach() {
        // Login button
        view.setOnLogin(e -> doLogin());

        // Sign up link
        view.setOnSignup(e -> {
            dispose();       // close Login frame first
            onGoToSignup.run();
        });

        // OAuth buttons
        view.setOnGoogle(e -> doOAuth("google"));
        view.setOnGitHub(e -> doOAuth("github"));
    }

    // Handles the normal email and password login
    private void doLogin() {
        String email = view.getEmail().trim();
        String password = view.getPassword();

        // Validation for missing inputs
        if (email.isEmpty() || password.isEmpty()) {
            view.showWarn("Please enter both email and password.", "Missing Information");
            return;
        }

        try {
            // Delegate auth to Authservice
            User user = authService.login(email, password);
            if (user != null) {
                // Stores the user in the session
                try { utils.Constants.Session.login(user); } catch (Throwable ignored) {}
                view.showInfo("Welcome " + (user.getName() != null ? user.getName() : "back") + "!", "Login Successful");
                dispose();  // Closes the login window
                onSuccess.run();    //Update the UI and go to the next screen
            } else {
                // Credentials not matching
                view.showError("Invalid email or password.", "Login Failed");
            }
        } catch (Exception ex) {
            view.showError("An error occurred: " + ex.getMessage(), "Error");
        }
    }

    // Handles an OAuth login
    private void doOAuth(String provider) {
        // Temp disables OAuth buttons so the user can't click it multiple times
        view.setOAuthEnabled(false);

        // Runs the OAuth flow off the EDT using Swing
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Chooses the correct OAuth method
                if ("google".equals(provider)) return authService.continueWithGoogle();
                if ("github".equals(provider)) return authService.continueWithGitHub();
                return null;
            }

            @Override
            protected void done() {
                // Re-enable the OAuth buttons
                view.setOAuthEnabled(true);
                try {
                    User user = get();
                    if (user != null) {
                        // Stores the user in the session
                        try { utils.Constants.Session.login(user); } catch (Throwable ignored) {}
                        dispose();
                        onSuccess.run();
                    } else {
                        view.showError("OAuth sign-in failed.", "Login Failed");
                    }
                } catch (Exception ex) {
                    // Any exceptions during OAuth
                    view.showError("OAuth error: " + ex.getMessage(), "Error");
                }
            }
        }.execute();
    }
}
