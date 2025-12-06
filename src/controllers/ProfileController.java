package controllers;

import models.User;
import ui.ProfileFrame;

import javax.swing.JOptionPane;

public class ProfileController extends BaseController<ProfileFrame> {

    // Callback to run after the user logs out
    private final Runnable onLogout;

    public ProfileController(ProfileFrame view, Runnable onLogout) {
        super(view);
        this.onLogout = onLogout;
        init(); // Initialize profile data and wire up actions
    }

    private void init() {
        // Try to load the current user from the Session
        try {
            User u = utils.Constants.Session.getCurrentUser();
            if (u != null) {
                // Show the user name and email
                view.setUser(u.getName(), u.getEmail());
            } else {
                // Fallback if nobody is logged in
                view.setUser("Not logged in", "Not logged in");
            }
        } catch (Throwable ignored) {
            // If anything goes wrong just empty fields
            view.setUser("—", "—");
        }

        // Wires the logout button
        view.setOnLogout(e -> {
            // Confirm dialog
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            // If the user decides to stay logged in
            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            // Clears the session
            try {
                utils.Constants.Session.logout();
            } catch (Throwable ignored) {
            }

            if (onLogout != null) {
                onLogout.run();
            }

            dispose();
        });
    }
}
