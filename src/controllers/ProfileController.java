package controllers;

import ui.ProfileFrame;

public class ProfileController extends BaseController<ProfileFrame> {
    private final Runnable onLogout; // refresh container auth UI

    public ProfileController(ProfileFrame view, Runnable onLogout) {
        super(view);
        this.onLogout = onLogout;
        attach();
    }

    private void attach() {
        view.setOnBack(e -> dispose());

        view.setOnLogout(e -> {
            try { utils.Constants.Session.logout(); } catch (Throwable ignored) {}
            if (onLogout != null) onLogout.run();
            dispose();
        });
    }
}
