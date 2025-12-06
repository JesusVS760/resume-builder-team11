package controllers;

import java.awt.Window;

public abstract class BaseController<V> {
    protected final V view;

    // Store the view reference when creating the controller
    protected BaseController(V view) { this.view = view; }

    // Show the view if it is a Window; no-op for pure panels.
    public void show() {
        if (view instanceof Window w) {
            // Centers the window on the screen
            w.setLocationRelativeTo(null);
            // Make the window visible
            w.setVisible(true);
        }
    }

    // Dispose the view if it is a Window; no-op for pure panels.
    public void dispose() {
        if (view instanceof Window w) { w.dispose(); }
    }

    // Convenience helper to get the current user ID or null if there isnt a user
    protected String getCurrentUserId() {
        try {
            if (utils.Constants.Session.isLoggedIn()) {
                models.User u = utils.Constants.Session.getCurrentUser();
                if (u != null) {
                    return u.getId();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}