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

    // Keep the initial id just in case, but we’ll prefer the Session value
    private final int initialUserId;

    private enum SortMode { DATE_DESC, NAME_ASC }
    private SortMode sortMode = SortMode.DATE_DESC;

    public SavedResumesController(SavedResumesPanel view,
                                  ResumeDAO resumeDAO,
                                  int userId) {
        super(view);
        this.resumeDAO = resumeDAO;
        this.initialUserId = userId;  // may be -1 if created before login

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
                    "Edit is not implemented yet.\nYou can wire this to open the resume in your builder.",
                    "Edit Resume",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        view.setOnDelete(resume -> {
            int uid = getCurrentUserId();
            if (uid <= 0) {
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
                    boolean ok = resumeDAO.deleteResume(resume.getId(), uid);
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
        int uid = getCurrentUserId();
        if (uid <= 0) {
            JOptionPane.showMessageDialog(
                    view,
                    "Please log in before uploading resumes.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload Resume");
        // Optional filter:
        // chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
        //         "Documents (PDF, DOC, DOCX)", "pdf", "doc", "docx"));

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
            int newId = saveResumeFile(selected, uid);
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
     * Get current user id from session (preferred), falling back to initialUserId.
     */
    private int getCurrentUserId() {
        try {
            if (utils.Constants.Session.isLoggedIn()) {
                var u = utils.Constants.Session.getCurrentUser();
                if (u != null) {
                    // User.getId() returns String -> parse to int
                    return Integer.parseInt(u.getId());
                }
            }
        } catch (Throwable ignored) {}
        return initialUserId;
    }

    /**
     * Copies file into an "uploads" folder and inserts a row in `resumes`.
     * Returns the new resume id.
     */
    private int saveResumeFile(File originalFile, int uid) throws IOException, SQLException {
        Path uploadsDir = Paths.get("uploads");
        if (Files.notExists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        String storedFileName = uid + "_" + System.currentTimeMillis() + "_" + originalFile.getName();
        Path dest = uploadsDir.resolve(storedFileName);

        Files.copy(originalFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        Resume resume = new Resume(uid, originalFile.getName(), dest.toString());
        return resumeDAO.saveResume(resume);
    }

    private void reload() {
        int uid = getCurrentUserId();
        if (uid <= 0) {
            view.showResumes(new ArrayList<>());
            return;
        }
        try {
            List<Resume> resumes;
            if (sortMode == SortMode.NAME_ASC) {
                resumes = resumeDAO.getResumesByUserOrderByName(uid);
            } else {
                resumes = resumeDAO.getResumesByUserOrderByDate(uid);
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
