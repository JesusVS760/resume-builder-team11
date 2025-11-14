package controllers;

import ui.UploadPanel;
import services.ResumeParserService;
import services.ResumeParserService.ParsedResume;
import services.ResumeTailoringService;

import dao.ResumeDAO;
import dao.TailoredResumeDAO;
import models.Resume;
import models.TailoredResume;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UploadController extends BaseController<UploadPanel> {
    private final ResumeParserService parser;
    private final ResumeTailoringService tailoringService;
    private final ResumeDAO resumeDAO;
    private final TailoredResumeDAO tailoredResumeDAO;
    private final int userId; // current logged-in user

    private ParsedResume lastParsed;

    public UploadController(UploadPanel view,
                            ResumeParserService parser,
                            ResumeTailoringService tailoringService,
                            ResumeDAO resumeDAO,
                            TailoredResumeDAO tailoredResumeDAO,
                            int userId) {
        super(view);
        this.parser = parser;
        this.tailoringService = tailoringService;
        this.resumeDAO = resumeDAO;
        this.tailoredResumeDAO = tailoredResumeDAO;
        this.userId = userId;
        attach();
    }

    private void attach() {
        // Auto-parse when a file is dropped/selected
        view.setOnFileDropped(file -> {
            if (file != null) {
                startParse(file, view.getJobDescription());
            }
        });

        // Parse when user clicks "Build tailored Resume"
        view.setOnBuild((file, jobDesc) -> {
            if (file == null) {
                view.showWarn("Please select or drop a resume file first.", "No file selected");
                return;
            }
            startParse(file, jobDesc);
        });
    }

    private void startParse(File file, String jobDesc) {
        view.setStatus("Reading file…");
        view.setBusy(true);
        view.setProgressValue(5);

        new SwingWorker<ParsedResume, Void>() {
            private int resumeId = -1;
            private String tailoredText; // may stay null if no jobDesc

            @Override
            protected ParsedResume doInBackground() throws Exception {
                updateProgress(15, "Validating file…");

                // 1) Save original resume file + DB record
                resumeId = saveResumeToDatabase(file);

                updateProgress(35, "Extracting text…");
                updateProgress(60, "Parsing sections…");

                ParsedResume parsed = parser.parseResumeComplete(file);

                // 2) Generate + save tailored resume if we have a job description
                if (jobDesc != null && !jobDesc.isBlank()) {
                    updateProgress(75, "Tailoring resume to job…");

                    // Use YOUR existing service method
                    tailoredText = tailoringService.tailorResume(parsed, jobDesc);

                    // Build model and persist
                    TailoredResume tr = new TailoredResume(
                            userId,
                            resumeId,
                            null,          // jobTitle (optional for now)
                            null,          // jobCompany
                            jobDesc,
                            tailoredText,
                            null           // filePath if you later export to PDF/DOCX
                    );

                    tailoredResumeDAO.saveTailoredResume(tr);
                }

                updateProgress(85, "Finalizing…");
                return parsed;
            }

            @Override
            protected void done() {
                try {
                    lastParsed = get();
                    updateProgress(100, "Done");
                    view.setBusy(false);

                    // Existing summary popup
                    showParsedSummary(lastParsed, file, jobDesc);

                    // Simple confirmation if we actually tailored & saved
                    if (tailoredText != null && !tailoredText.isBlank()) {
                        JOptionPane.showMessageDialog(
                                view,
                                "A tailored resume was generated and saved for this job description.",
                                "Tailored Resume Saved",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                } catch (Exception ex) {
                    view.setBusy(false);
                    view.setProgressValue(0);
                    view.setStatus("Ready");
                    view.showError("Resume processing failed: " + ex.getMessage(), "Error");
                }
            }

            private void updateProgress(int pct, String status) {
                SwingUtilities.invokeLater(() -> {
                    view.setProgressValue(pct);
                    view.setStatus(status);
                });
            }
        }.execute();
    }

    /**
     * Copies the original file into an "uploads" folder and inserts a row in `resumes`,
     * returning the new resume's database id.
     */
    private int saveResumeToDatabase(File originalFile) throws IOException, SQLException {
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

    private void showParsedSummary(ParsedResume parsed, File file, String jobDesc) {
        String full = parsed.getFullText() == null ? "" : parsed.getFullText();
        Map<String, String> sections = parsed.getSections();
        List<String> exps = parsed.getExperiences();

        StringBuilder html = new StringBuilder(2048);
        html.append("<html><body style='font-family:Segoe UI, sans-serif; font-size:12px; color:#111;'>");

        html.append("<h3 style='margin:0 0 8px 0;'>Parsed Resume Summary</h3>");
        html.append("<div style='margin:2px 0;'><b>File:</b> ").append(escape(file.getName())).append("</div>");
        html.append("<div style='margin:2px 0;'><b>Characters:</b> ").append(full.length()).append("</div>");
        html.append("<div style='margin:2px 0;'><b>Words:</b> ").append(wordCount(full)).append("</div>");

        // Sections
        int sectionCount = sections == null ? 0 : sections.size();
        html.append("<div style='margin-top:10px;'><b>Sections (").append(sectionCount).append(")</b></div>");
        if (sectionCount > 0) {
            html.append("<ul style='margin:4px 0 8px 20px;'>");
            for (Map.Entry<String, String> e : sections.entrySet()) {
                String key = capitalize(e.getKey());
                String val = e.getValue() == null ? "" : e.getValue();
                html.append("<li>")
                        .append(escape(key))
                        .append(" — ")
                        .append(wordCount(val))
                        .append(" words</li>");
            }
            html.append("</ul>");
        } else {
            html.append("<div style='color:#666;'>No common sections detected.</div>");
        }

        // Experiences (show first 2 snippets)
        if (exps != null && !exps.isEmpty()) {
            html.append("<div style='margin-top:8px;'><b>Experiences (")
                    .append(exps.size())
                    .append(")</b></div><ol style='margin:4px 0 0 20px;'>");
            for (int i = 0; i < Math.min(2, exps.size()); i++) {
                html.append("<li>")
                        .append(escape(truncate(exps.get(i), 260)))
                        .append("</li>");
            }
            if (exps.size() > 2) {
                html.append("<li style='color:#666;'>…and ").append(exps.size() - 2).append(" more</li>");
            }
            html.append("</ol>");
        }

        if (jobDesc != null && !jobDesc.isBlank()) {
            html.append("<div style='margin-top:10px;'><b>Job description length:</b> ")
                    .append(jobDesc.length()).append(" chars (")
                    .append(wordCount(jobDesc)).append(" words)")
                    .append("</div>");
        }

        html.append("</body></html>");

        JEditorPane pane = new JEditorPane("text/html", html.toString());
        pane.setEditable(false);
        pane.setOpaque(true);
        pane.setBackground(Color.WHITE);
        pane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroller = new JScrollPane(pane);
        scroller.setPreferredSize(new Dimension(560, 420));
        scroller.setBorder(null);

        JOptionPane.showMessageDialog(
                view,
                scroller,
                "Parsed Resume Summary",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static int wordCount(String s) {
        if (s == null) return 0;
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return 0;
        String[] parts = trimmed.split("\\s+");
        return parts.length;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public ParsedResume getLastParsed() {
        return lastParsed;
    }
}
