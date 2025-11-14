package controllers;

import dao.ResumeDAO;
import models.Resume;
import ui.SavedResumesPanel;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SavedResumesController extends BaseController<SavedResumesPanel> {

    private final ResumeDAO resumeDAO;

    private enum SortMode { DATE_DESC, NAME_ASC }
    private SortMode sortMode = SortMode.DATE_DESC;

    public SavedResumesController(SavedResumesPanel view, ResumeDAO resumeDAO) {
        super(view);
        this.resumeDAO = resumeDAO;

        attach();
        reload();
    }

    private void attach() {
        // Upload button: open file chooser and save to DB
        view.setOnUpload(this::handleUploadClicked);

        view.setOnSortByDate(() -> {
            sortMode = SortMode.DATE_DESC;
            reload();
        });

        view.setOnSortByName(() -> {
            sortMode = SortMode.NAME_ASC;
            reload();
        });

        view.setOnEdit(resume -> {
            JOptionPane.showMessageDialog(
                    view,
                    "Edit is not implemented yet.",
                    "Edit Resume",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        view.setOnDelete(resume -> {
            String userId = getCurrentUserId();
            if (userId == null || userId.isBlank()) {
                JOptionPane.showMessageDialog(
                        view,
                        "Please log in before deleting resumes.",
                        "Not Logged In",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    view,
                    "Delete this resume?\n" + resume.getFileName(),
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    boolean ok = resumeDAO.deleteResume(resume.getId(), userId);
                    if (!ok) {
                        JOptionPane.showMessageDialog(
                                view,
                                "Could not delete the resume.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                    reload();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            view,
                            "Error deleting resume: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    private void handleUploadClicked() {
        boolean loggedIn = false;
        try {
            loggedIn = utils.Constants.Session.isLoggedIn();
        } catch (Throwable ignored) {}

        if (!loggedIn) {
            JOptionPane.showMessageDialog(
                    view,
                    "Please log in before uploading resumes.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String userId = getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            JOptionPane.showMessageDialog(
                    view,
                    "You're logged in, but we couldn't determine your account id.\n" +
                            "Make sure UserDAO sets user.setId(...) from the database.",
                    "Account Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload Resume");

        int result = chooser.showOpenDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled
        }

        File selected = chooser.getSelectedFile();
        if (selected == null || !selected.exists()) {
            JOptionPane.showMessageDialog(
                    view,
                    "Selected file is not valid.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            int newId = saveResumeFile(selected, userId);
            if (newId > 0) {
                JOptionPane.showMessageDialog(
                        view,
                        "Resume uploaded and saved.",
                        "Upload Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
                reload();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    view,
                    "Failed to upload resume: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Get current user id from session as a String (e.g. "U5").
     */
    private String getCurrentUserId() {
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

    /**
     * Copies file into an "uploads" folder and inserts a row in `resumes`.
     * Returns the new resume id.
     */
    private int saveResumeFile(File originalFile, String userId) throws IOException, SQLException {
        Path uploadsDir = Paths.get("uploads");
        if (Files.notExists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        String storedFileName = userId + "_" + System.currentTimeMillis() + "_" + originalFile.getName();
        Path dest = uploadsDir.resolve(storedFileName);

        Files.copy(originalFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        Resume resume = new Resume(userId, originalFile.getName(), dest.toString());
        return resumeDAO.saveResume(resume);
    }

    private void reload() {
        boolean loggedIn = false;
        try {
            loggedIn = utils.Constants.Session.isLoggedIn();
        } catch (Throwable ignored) {}

        if (!loggedIn) {
            view.showResumes(new ArrayList<>());
            return;
        }

        String userId = getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            view.showResumes(new ArrayList<>());
            return;
        }

        try {
            List<Resume> resumes;
            if (sortMode == SortMode.NAME_ASC) {
                resumes = resumeDAO.getResumesByUserOrderByName(userId);
            } else {
                resumes = resumeDAO.getResumesByUserOrderByDate(userId);
            }
            view.showResumes(resumes);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    view,
                    "Error loading resumes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void refresh() {
        reload();
    }
}
