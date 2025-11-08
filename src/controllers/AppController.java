package controllers;

import services.AuthService;
import services.TwilioService;

import ui.ResumeBuilderContainer;
import ui.UploadPanel;
import ui.LoginFrame;
import ui.SignupFrame;

public class AppController extends BaseController<ResumeBuilderContainer> {
    private final AuthService authService;
    private final TwilioService twilioService;

    private UploadController uploadController;

    public AppController(ResumeBuilderContainer container,
                         AuthService authService,
                         TwilioService twilioService) {
        super(container);
        this.authService = authService;
        this.twilioService = twilioService;

        wireNav();
        wireUpload();
    }

    // Navigation wiring
    private void wireNav() {
        view.setOnNavHome(e -> view.showHome());
        view.setOnNavBuild(e -> view.showBuild());
        view.setOnNavSaved(e -> view.showSaved());
        view.setOnNavSettings(e -> view.showSettings());
        view.setOnNavProfile(e -> {
            if (isLoggedIn()) {
                openProfile();    // switch card, not a new window
            } else {
                openLogin();
            }
        });
    }

    // Upload wiring
    private void wireUpload() {
        UploadPanel up = view.getUploadPanel();
        if (up != null && uploadController == null) {
            uploadController = new UploadController(view.getUploadPanel(), new services.ResumeParserService());
        }
    }

    // Auth helpers
    private boolean isLoggedIn() {
        try { return utils.Constants.Session.isLoggedIn(); }
        catch (Throwable t) { return false; }
    }

    private void onLogout() {
        try { utils.Constants.Session.logout(); } catch (Throwable ignored) {}
        view.updateAuthUIPublic();
        view.showHome();
    }

    public void openLogin() {
        var c = new LoginController(
                new LoginFrame(),
                authService,
                () -> {
                    view.updateAuthUIPublic();
                    view.showProfile();
                    try {
                        var u = utils.Constants.Session.getCurrentUser();
                        view.updateProfileView(u);
                    } catch (Throwable ignored) {}
                },
                this::openSignup
        );
        c.show();
    }

    public void openSignup() {
        var c = new SignupController(
                new SignupFrame(),
                authService,
                twilioService,
                this::openLogin
        );
        c.show();
    }

    public void openProfile() {
        view.showProfile();
        try {
            var u = utils.Constants.Session.getCurrentUser();
            view.updateProfileView(u);
        } catch (Throwable ignored) {}
    }
}
