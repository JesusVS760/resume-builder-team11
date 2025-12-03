package controllers;

import services.AuthService;
import services.TwilioService;
import ui.SignupFrame;
import ui.VerificationFrame;

import javax.swing.SwingWorker;

public class SignupController extends BaseController<SignupFrame> {
    private final AuthService authService;
    private final TwilioService twilioService;
    private final Runnable onBackToLogin;

    public SignupController(SignupFrame view,
                            AuthService authService,
                            TwilioService twilioService,
                            Runnable onBackToLogin) {
        super(view);
        this.authService = authService;
        this.twilioService = twilioService;
        this.onBackToLogin = onBackToLogin;
        attach();
    }

    private void attach() {
        view.setOnSignup(e -> doSignup());
        view.setOnBackToLogin(e -> {
            dispose();
            onBackToLogin.run();
        });

        view.setOnGoogle(e -> doOAuth("google"));
        view.setOnGitHub(e -> doOAuth("github"));
    }

    private void doSignup() {
        String email = view.getEmail().trim();
        String pw = view.getPassword();
        String confirm = view.getConfirmPassword();

        if (email.isEmpty() || pw.isEmpty() || confirm.isEmpty()) {
            view.showWarn("Please fill in all fields.", "Missing Information");
            return;
        }
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
                new VerificationController(vf, authService, twilioService, () -> {
                    dispose();
                    onBackToLogin.run();
                });
                vf.setVisible(true);

            } else {
                view.showError("Failed to start sign up. Please try again.", "Sign Up Failed");
            }
        } catch (Exception ex) {
            view.showError("An error occurred: " + ex.getMessage(), "Error");
        }
    }

    private void doOAuth(String provider) {
        view.setOAuthEnabled(false);
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
                view.setOAuthEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        dispose();
                        onBackToLogin.run();
                    } else {
                        view.showError("OAuth sign-up failed.", "Sign Up Failed");
                    }
                } catch (Exception ex) {
                    view.showError("OAuth error: " + ex.getMessage(), "Error");
                }
            }
        }.execute();
    }
}
