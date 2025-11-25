package controllers;

import ui.ProfileFrame;

import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfileController extends BaseController<ProfileFrame> {
    private final Runnable onLogout; // refresh container auth UI

    public ProfileController(ProfileFrame view, Runnable onLogout) {
        super(view);
        this.onLogout = onLogout;
        attach();
    }

    private void attach() {
        // Back button behavior stays the same
        view.setOnBack(e -> dispose());

        // Button responsiveness
        JButton logoutBtn = view.getLogoutButton();
        if (logoutBtn != null) {
            final Color base = logoutBtn.getBackground();
            final Color hover = (base != null) ? base.brighter() : null;

            logoutBtn.setOpaque(true);
            logoutBtn.setContentAreaFilled(true);
            logoutBtn.setFocusPainted(false);

            logoutBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (hover != null) {
                        logoutBtn.setBackground(hover);  // a little lighter
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (base != null) {
                        logoutBtn.setBackground(base);   // back to original
                    }
                }
            });
        }

        // Logout with confirmation dialog
        view.setOnLogout(e -> {
            int result = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                // User cancels
                return;
            }

            try { utils.Constants.Session.logout(); } catch (Throwable ignored) {}
            if (onLogout != null) onLogout.run();
            dispose();
        });
    }

}
