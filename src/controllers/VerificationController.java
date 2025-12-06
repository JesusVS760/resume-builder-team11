package controllers;

import services.AuthService;
import services.TwilioService;
import ui.VerificationFrame;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Color;

/*
     Controller for the verification window.
     Handles sending a verification code (via Twilio) and completing signup
     after the user enters the correct code.
*/
public class VerificationController extends BaseController<VerificationFrame> {
    // Service used to complete signup in the backend
    private final AuthService authService;
    // Service used to send and verify codes (e.g., email/SMS via Twilio)
    private final TwilioService twilioService;
    // Callback to run after successful verification (e.g., return to Login)
    private final Runnable onVerified;

    public VerificationController(VerificationFrame view,
                                  AuthService authService,
                                  TwilioService twilioService,
                                  Runnable onVerified) {
        super(view);
        this.authService = authService;
        this.twilioService = twilioService;
        this.onVerified = onVerified;
        attach();

        // Auto-send code if in auto-send mode (email already provided)
        if (view.isAutoSendMode()) {
            SwingUtilities.invokeLater(this::autoSendCode);
        }
    }

    // Wire up UI event handlers
    private void attach() {
        view.setOnSendCode(e -> doSendCode());
        view.setOnVerifyCode(e -> doVerify());
    }

    /*
         Automatically send a verification code when the frame opens,
         if the email is already known (auto-send mode).
    */
    private void autoSendCode() {
        final String email = safe(view.getEmail());
        if (email.isEmpty()) {
            return;
        }

        view.setStatusText("Sending verification code...");
        view.setInputsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                twilioService.sendVerificationCode(email);
                return true;
            }

            @Override
            protected void done() {
                view.setInputsEnabled(true);
                try {
                    if (get()) {
                        view.setStatusText("Verification code sent!");
                        view.setStatusColor(new Color(0, 180, 0)); // Green
                    } else {
                        view.setStatusText("Failed to send code. Try again.");
                        view.setStatusColor(Color.RED);
                    }
                } catch (Exception ex) {
                    view.setStatusText("Failed: " + ex.getMessage());
                    view.setStatusColor(Color.RED);
                }
            }
        }.execute();
    }

    /*
        Manually send a verification code when the user clicks "Send Code".
    */
    private void doSendCode() {
        final String email = safe(view.getEmail());
        if (email.isEmpty()) {
            view.showError("Please enter your email.", "Missing Email");
            return;
        }

        view.setInputsEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                twilioService.sendVerificationCode(email);
                return true;
            }

            @Override
            protected void done() {
                view.setInputsEnabled(true);
                try {
                    if (get()) {
                        view.showInfo("Verification code sent!", "Sent");
                    } else {
                        view.showError("Failed to send verification code.", "Error");
                    }
                } catch (Exception ex) {
                    view.showError("Failed to send code: " + ex.getMessage(), "Error");
                }
            }
        }.execute();
    }


    // Verify the entered code and, if valid, complete the signup using the token.
    private void doVerify() {
        final String code = safe(view.getEnteredCode());
        if (!code.matches("^\\d{6}$")) {
            view.showError("Verification code must be 6 digits.", "Invalid Code");
            return;
        }

        // Require a token from the signup step
        final String token = safe(view.getVerificationToken());
        if (token.isEmpty()) {
            view.showError(
                    "Missing verification token from signup. Please restart the signup process.",
                    "Missing Token"
            );
            return;
        }

        view.setInputsEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // First confirm the code with Twilio
                boolean codeOk = twilioService.verifyCode(code);
                if (!codeOk) return false;
                // Then finalize the signup in the auth service
                return authService.completeSignup(token);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        view.showInfo("Account verified! You can now log in.", "Verified");
                        dispose();
                        onVerified.run();
                    } else {
                        view.setInputsEnabled(true);
                        view.showError("Verification failed. Check your code or try again.", "Error");
                    }
                } catch (Exception ex) {
                    view.setInputsEnabled(true);
                    view.showError("Verification error: " + ex.getMessage(), "Error");
                }
            }
        }.execute();
    }

    // Null-safe trim helper
    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
