package controllers;


import javax.swing.*;
import java.awt.*;


public abstract class BaseController<V> {
    protected final V view;
    protected BaseController(V view) { this.view = view; }

    /** Show the view if it is a Window; no-op for pure panels. */
    public void show() {
        if (view instanceof Window w) {
            w.setLocationRelativeTo(null);
            w.setVisible(true);
        }
    }

    /** Dispose the view if it is a Window; no-op for pure panels. */
    public void dispose() {
        if (view instanceof Window w) { w.dispose(); }
    }

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