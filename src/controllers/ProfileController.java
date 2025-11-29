package controllers;

import models.User;
import ui.ProfileFrame;

import javax.swing.JOptionPane;

public class ProfileController extends BaseController<ProfileFrame> {

    private final Runnable onLogout;

    public ProfileController(ProfileFrame view, Runnable onLogout) {
        super(view);
        this.onLogout = onLogout;
        init();
    }

    private void init() {
        try {
            User u = utils.Constants.Session.getCurrentUser();
            if (u != null) {
                view.setUser(u.getName(), u.getEmail());
            } else {
                view.setUser("Not logged in", "Not logged in");
            }
        } catch (Throwable ignored) {
            view.setUser("—", "—");
        }

        view.setOnLogout(e -> {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

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
