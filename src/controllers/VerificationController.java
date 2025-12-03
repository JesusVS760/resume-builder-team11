package controllers;

import services.AuthService;
import services.TwilioService;
import ui.VerificationFrame;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Color;

public class VerificationController extends BaseController<VerificationFrame> {
    private final AuthService authService;
    private final TwilioService twilioService;
    private final Runnable onVerified; // e.g., open Login

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

    private void attach() {
        view.setOnSendCode(e -> doSendCode());
        view.setOnVerifyCode(e -> doVerify());
    }
    
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
                boolean codeOk = twilioService.verifyCode(code);
                if (!codeOk) return false;
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

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
