package controllers;

import services.AuthService;
import services.TwilioService;
import services.ResumeParserService;
import services.ResumeAnalyzeService;
import dao.ResumeDAO;
import dao.AnalyzedResumeDAO;

import ui.*;

public class AppController extends BaseController<ResumeAnalyzingContainer> {
    private final AuthService authService;
    private final TwilioService twilioService;

    private UploadController uploadController;
    private SavedResumesController savedResumesController;

    public AppController(ResumeAnalyzingContainer container,
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
            ResumeAnalyzeService tailoringService = new ResumeAnalyzeService();

            // DAOs
            ResumeDAO resumeDAO = new ResumeDAO();
            AnalyzedResumeDAO tailoredResumeDAO = new AnalyzedResumeDAO();

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

    public void openLogin() {
        var c = new LoginController(
                new LoginFrame(),
                authService,
                () -> {
                    // on successful login:
                    view.updateAuthUIPublic();
                    openProfile();
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
        // Show the PROFILE card inside ResumeBuilderContainer
        view.showProfile();

        // Make sure the email/name on the profile card are up to date
        try {
            var u = utils.Constants.Session.getCurrentUser();
            view.updateProfileView(u);
        } catch (Throwable ignored) { }
    }
}
