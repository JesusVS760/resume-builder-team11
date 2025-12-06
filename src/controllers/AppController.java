package controllers;

import services.AuthService;
import services.TwilioService;
import services.ResumeParserService;
import services.ResumeAnalyzeService;
import dao.ResumeDAO;
import dao.AnalyzedResumeDAO;

import ui.*;

// Controller for the app
public class AppController extends BaseController<ResumeAnalyzingContainer> {
    // Authentication related services
    private final AuthService authService;
    private final TwilioService twilioService;

    // Child controllers
    private UploadController uploadController;
    private SavedResumesController savedResumesController;

    public AppController(ResumeAnalyzingContainer container,
                         AuthService authService,
                         TwilioService twilioService) {
        super(container);
        this.authService = authService;
        this.twilioService = twilioService;

        // Wires the main sections when the app controller is created
        wireNav();
        wireUpload();
        wireSaved();
    }
    // =================
    // Navigation wiring
    // =================

    //Navigation Panel stuff
    private void wireNav() {
        // Home button
        view.setOnNavHome(e -> view.showHome());

        // Analyze Resume button
        view.setOnNavBuild(e -> view.showBuild());

        // Saved Resumes button
        view.setOnNavSaved(e -> {
            view.showSaved();
            if (savedResumesController != null) {
                // This just makes sure that the saved resumes list is updated
                savedResumesController.refresh();
            }
        });

        // Profile button
        view.setOnNavProfile(e -> {
            if (isLoggedIn()) {
                // If the user is logged in they can access their profile
                openProfile();
            } else {
                // If they are not logged in the login frame pops up
                openLogin();
            }
        });
    }

    // Upload wiring
    private void wireUpload() {
        // Gets the upload panel from the main container
        UploadPanel up = view.getUploadPanel();
        if (up != null && uploadController == null) {
            // Services for parsing
            ResumeParserService parser = new ResumeParserService();
            ResumeAnalyzeService analyzeService = new ResumeAnalyzeService();

            // DAOs for both forms of resumes
            ResumeDAO resumeDAO = new ResumeDAO();
            AnalyzedResumeDAO analyzedResumeDAO = new AnalyzedResumeDAO();

            // Controller that handles the upload
            uploadController = new UploadController(
                    up,
                    parser,
                    analyzeService,
                    resumeDAO,
                    analyzedResumeDAO
            );
        }
    }

    // Saved resumes wiring
    private void wireSaved() {
        // Gets the saved resumes panel from the main container
        SavedResumesPanel savedPanel = view.getSavedPanel();
        if (savedPanel != null && savedResumesController == null) {
            // SavedResumesController reads the current user from Session itself
            savedResumesController = new SavedResumesController(
                    savedPanel,
                    new ResumeDAO()
            );
        }
    }
    // ============
    // Auth helpers
    // ============

    // helper to check if user is currently logged in
    private boolean isLoggedIn() {
        try {
            return utils.Constants.Session.isLoggedIn();
        } catch (Throwable t) {
            // For any errors or crashes
            return false;
        }
    }

    // Opens the login window
    public void openLogin() {
        var c = new LoginController(
                new LoginFrame(),
                authService,
                () -> {
                    // on successful login:
                    // 1. Update the auth UI
                    view.updateAuthUIPublic();
                    // 2. Opens the profile page
                    openProfile();
                },
                this::openSignup // if the user signs up
        );
        // show the login frame
        c.show();
    }

    // Opens the signup window
    public void openSignup() {
        var c = new SignupController(
                new SignupFrame(),
                authService,
                twilioService,
                this::openLogin
        );
        // Shows the signup frame
        c.show();
    }

    // Show the profile page in the main container
    public void openProfile() {
        // Show the PROFILE card inside ResumeAnalyzerContainer
        view.showProfile();

        // Makes sure the email/name on the profile card are up to date
        try {
            var u = utils.Constants.Session.getCurrentUser();
            view.updateProfileView(u);
        } catch (Throwable ignored) {
            // For crashes or errors
        }
    }
}
