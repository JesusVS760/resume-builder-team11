package controllers;

import models.User;
import services.AuthService;
import ui.LoginFrame;

import javax.swing.SwingWorker;

public class LoginController extends BaseController<LoginFrame> {
    private final AuthService authService;
    private final Runnable onSuccess;
    private final Runnable onGoToSignup;

    public LoginController(LoginFrame view,
                           AuthService authService,
                           Runnable onSuccess,
                           Runnable onGoToSignup) {
        super(view);
        this.authService = authService;
        this.onSuccess = onSuccess;
        this.onGoToSignup = onGoToSignup;
        attach();
    }


    private void attach() {
        view.setOnLogin(e -> doLogin());
        view.setOnSignup(e -> {
            dispose();       // close Login frame first
            onGoToSignup.run();
        });
        view.setOnGoogle(e -> doOAuth("google"));
        view.setOnGitHub(e -> doOAuth("github"));
    }

    private void doLogin() {
        String email = view.getEmail().trim();
        String password = view.getPassword();

        if (email.isEmpty() || password.isEmpty()) {
            view.showWarn("Please enter both email and password.", "Missing Information");
            return;
        }

        try {
            User user = authService.login(email, password);
            if (user != null) {
                try { utils.Constants.Session.login(user); } catch (Throwable ignored) {}
                view.showInfo("Welcome " + (user.getName() != null ? user.getName() : "back") + "!", "Login Successful");
                dispose();
                onSuccess.run();
            } else {
                view.showError("Invalid email or password.", "Login Failed");
            }
        } catch (Exception ex) {
            view.showError("An error occurred: " + ex.getMessage(), "Error");
        }
    }

    private void doOAuth(String provider) {
        view.setOAuthEnabled(false);
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                if ("google".equals(provider)) return authService.continueWithGoogle();
                if ("github".equals(provider)) return authService.continueWithGitHub();
                return null;
            }

            @Override
            protected void done() {
                view.setOAuthEnabled(true);
                try {
                    User user = get();
                    if (user != null) {
                        try { utils.Constants.Session.login(user); } catch (Throwable ignored) {}
                        dispose();
                        onSuccess.run();
                    } else {
                        view.showError("OAuth sign-in failed.", "Login Failed");
                    }
                } catch (Exception ex) {
                    view.showError("OAuth error: " + ex.getMessage(), "Error");
                }
            }
        }.execute();
    }
}
