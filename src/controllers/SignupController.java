package controllers;

import services.AuthService;
import services.TwilioService;
import ui.SignupFrame;
import ui.VerificationFrame;

import javax.swing.SwingWorker;


// Controller for the signup window.
public class SignupController extends BaseController<SignupFrame> {
    // Service used for sign-up and auth operations
    private final AuthService authService;
    // Service used to send verification codes via SMS
    private final TwilioService twilioService;
    // Callback to return to the login screen
    private final Runnable onBackToLogin;

    public SignupController(SignupFrame view,
                            AuthService authService,
                            TwilioService twilioService,
                            Runnable onBackToLogin) {
        super(view);
        this.authService = authService;
        this.twilioService = twilioService;
        this.onBackToLogin = onBackToLogin;
        attach(); // wire up button actions
    }

    // Attach all event listeners to the signup view
    private void attach() {
        // Main "Sign Up" button -> attempt to sign up
        view.setOnSignup(e -> doSignup());

        // "Back to login" link/button -> close and go back to login screen
        view.setOnBackToLogin(e -> {
            dispose();
            onBackToLogin.run();
        });

        // OAuth sign-up buttons
        view.setOnGoogle(e -> doOAuth("google"));
        view.setOnGitHub(e -> doOAuth("github"));
    }

    // Handle the normal email/password sign-up flow
    private void doSignup() {
        String email = view.getEmail().trim();
        String pw = view.getPassword();
        String confirm = view.getConfirmPassword();

        // Simple validation for empty fields
        if (email.isEmpty() || pw.isEmpty() || confirm.isEmpty()) {
            view.showWarn("Please fill in all fields.", "Missing Information");
            return;
        }
        // Check that password and confirm password match
        if (!pw.equals(confirm)) {
            view.showWarn("Passwords do not match.", "Password Mismatch");
            return;
        }

        try {
            // initiateSignup returns a verification TOKEN, not a boolean
            String token = authService.initiateSignup(email, pw, "");

            if (token != null && !token.isEmpty()) {
                // Open verification frame - it will auto-send the code
                VerificationFrame vf = new VerificationFrame(email, token);

                // Controller for verification; on success, go back to login
                new VerificationController(vf, authService, twilioService, () -> {
                    dispose();
                    onBackToLogin.run();
                });

                vf.setVisible(true);
            } else {
                // If no token was returned, sign-up could not be started
                view.showError("Failed to start sign up. Please try again.", "Sign Up Failed");
            }
        } catch (Exception ex) {
            // Catch any unexpected error from auth layer
            view.showError("An error occurred: " + ex.getMessage(), "Error");
        }
    }

    // Handle OAuth-based sign-up (Google / GitHub)
    private void doOAuth(String provider) {
        // Disable OAuth buttons while the async operation is running
        view.setOAuthEnabled(false);

        // Run OAuth call in a background thread
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if ("google".equals(provider)) {
                    return authService.continueWithGoogle() != null;
                } else if ("github".equals(provider)) {
                    return authService.continueWithGitHub() != null;
                }
                return false;
            }

            @Override
            protected void done() {
                // Re-enable OAuth buttons when background work is done
                view.setOAuthEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        // On success, close signup and return to login screen
                        dispose();
                        onBackToLogin.run();
                    } else {
                        view.showError("OAuth sign-up failed.", "Sign Up Failed");
                    }
                } catch (Exception ex) {
                    // Any exception in the OAuth process is shown as an error
                    view.showError("OAuth error: " + ex.getMessage(), "Error");
                }
            }
        }.execute();
    }
}
