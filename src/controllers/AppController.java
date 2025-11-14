package controllers;

import services.AuthService;
import services.TwilioService;
import services.ResumeParserService;
import services.ResumeTailoringService;
import dao.ResumeDAO;
import dao.TailoredResumeDAO;

import ui.ResumeBuilderContainer;
import ui.UploadPanel;
import ui.LoginFrame;
import ui.SignupFrame;
import ui.SavedResumesPanel;

public class AppController extends BaseController<ResumeBuilderContainer> {
    private final AuthService authService;
    private final TwilioService twilioService;

    private UploadController uploadController;
    private SavedResumesController savedResumesController;

    public AppController(ResumeBuilderContainer container,
                         AuthService authService,
                         TwilioService twilioService) {
        super(container);
        this.authService = authService;
        this.twilioService = twilioService;

        wireNav();
        wireUpload();
        wireSaved();
    }

    // Navigation wiring
    private void wireNav() {
        view.setOnNavHome(e -> view.showHome());
        view.setOnNavBuild(e -> view.showBuild());
        view.setOnNavSaved(e -> {
            view.showSaved();
            if (savedResumesController != null) {
                savedResumesController.refresh();
            }
        });
        view.setOnNavSettings(e -> view.showSettings());
        view.setOnNavProfile(e -> {
            if (isLoggedIn()) {
                openProfile();
            } else {
                openLogin();
            }
        });
    }

    // Upload wiring
    private void wireUpload() {
        UploadPanel up = view.getUploadPanel();
        if (up != null && uploadController == null) {
            // Services
            ResumeParserService parser = new ResumeParserService();
            ResumeTailoringService tailoringService = new ResumeTailoringService();

            // DAOs
            ResumeDAO resumeDAO = new ResumeDAO();
            TailoredResumeDAO tailoredResumeDAO = new TailoredResumeDAO();

            uploadController = new UploadController(
                    up,
                    parser,
                    tailoringService,
                    resumeDAO,
                    tailoredResumeDAO
            );
        }
    }

    // Saved resumes wiring
    private void wireSaved() {
        SavedResumesPanel savedPanel = view.getSavedPanel();
        if (savedPanel != null && savedResumesController == null) {
            // SavedResumesController now reads the current user from Session itself
            savedResumesController = new SavedResumesController(
                    savedPanel,
                    new ResumeDAO()
            );
        }
    }

    // Auth helpers
    private boolean isLoggedIn() {
        try {
            return utils.Constants.Session.isLoggedIn();
        } catch (Throwable t) {
            return false;
        }
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
