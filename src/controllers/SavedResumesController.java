package controllers;

import dao.ResumeDAO;
import models.Resume;
import services.ExportService;
import ui.ResumeEditFrame;
import ui.SavedResumesPanel;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SavedResumesController extends BaseController<SavedResumesPanel> {

    // Data access object for reading and writing resumes in the database
    private final ResumeDAO resumeDAO;

    // Service responsible for exporting resumes (PDF / DOCX)
    private final ExportService exportService;

    // Sorting options for the list
    private enum SortMode { DATE_DESC, NAME_ASC }
    private SortMode sortMode = SortMode.DATE_DESC; // default sort

    public SavedResumesController(SavedResumesPanel view, ResumeDAO resumeDAO) {
        super(view);
        this.resumeDAO = resumeDAO;
        this.exportService = new ExportService();

        attach(); // wire UI callbacks
        reload(); // initial load of resumes
    }

    // Wire up all the callbacks from the saved panel
    private void attach() {
        // Upload button: open file chooser and save to DB
        view.setOnUpload(this::handleUploadClicked);

        // Sort by date
        view.setOnSortByDate(() -> {
            sortMode = SortMode.DATE_DESC;
            reload();
        });

        // Sort by alphabet
        view.setOnSortByName(() -> {
            sortMode = SortMode.NAME_ASC;
            reload();
        });

        // Edit selected resume
        view.setOnEdit(resume -> handleEdit(resume));

        // Delete selected resume
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

        // Export as pdf
        view.setOnExportPdf(resume -> handleExportPdf(resume));

        // Export as DOCX
        view.setOnExportDocx(resume -> handleExportDocx(resume));
    }

    // Handle editing a saved resume
    private void handleEdit(Resume resume) {
        String userId = getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            JOptionPane.showMessageDialog(
                    view,
                    "Please log in before editing resumes.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Check if file exists
        File file = new File(resume.getFilePath());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(
                    view,
                    "The resume file could not be found:\n" + resume.getFilePath(),
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Open the edit frame
        ResumeEditFrame editFrame = new ResumeEditFrame(resume);

        // Set up save callback
        editFrame.setOnSaveCallback(r -> {
            try {
                String content = editFrame.getCurrentContent();
                boolean saved = saveEditedContent(r, content, userId);

                if (saved) {
                    editFrame.notifySaveSuccess();
                    reload(); // Refresh the list
                } else {
                    editFrame.notifySaveError("Could not save the file.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                editFrame.notifySaveError(e.getMessage());
            }
        });

        editFrame.setVisible(true);
    }

    // Saves edited content to the existing file
    private boolean saveEditedContent(Resume resume, String content, String userId) {
        try {
            String originalPath = resume.getFilePath();
            String fileType = getFileExtension(originalPath);

            // Overwrite the existing file with the same format
            boolean success;
            if ("pdf".equalsIgnoreCase(fileType)) {
                success = saveEditedPdfFile(originalPath, content);
            } else {
                success = saveEditedDocxFile(originalPath, content);
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //Saves edited content to an existing PDF file (overwrites)
    private boolean saveEditedPdfFile(String filePath, String content) {
        try {
            // Create PDF with the edited content using PDFBox
            org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();

            // Clean content - remove control characters and replace special Unicode characters
            // that Helvetica font doesn't support
            content = content.replace("\r", "").replace("\t", "    ");
            content = content.replaceAll("[\\p{Cntrl}&&[^\n]]", "");
            
            // Replace common bullet points and special characters with ASCII equivalents
            content = content.replace("\u25A0", "-");  // blacksquare ■
            content = content.replace("\u25AA", "-");  // small blacksquare ▪
            content = content.replace("\u25CF", "-");  // bullet ●
            content = content.replace("\u2022", "-");  // bullet •
            content = content.replace("\u25E6", "-");  // white bullet ◦
            content = content.replace("\u25B8", "-");  // right arrow ▸
            content = content.replace("\u25BA", "-");  // right pointer ►
            content = content.replace("\u2192", "->");  // arrow →
            content = content.replace("\u2190", "<-");  // left arrow ←
            content = content.replace("\u2013", "-");  // en dash –
            content = content.replace("\u2014", "-");  // em dash —
            content = content.replace("\u2018", "'");  // left single quote '
            content = content.replace("\u2019", "'");  // right single quote '
            content = content.replace("\u201C", "\""); // left double quote "
            content = content.replace("\u201D", "\""); // right double quote "
            content = content.replace("\u2026", "..."); // ellipsis …
            content = content.replace("\u00A9", "(c)"); // copyright ©
            content = content.replace("\u00AE", "(R)"); // registered ®
            content = content.replace("\u2122", "(TM)"); // trademark ™
            content = content.replace("\u00B0", " deg"); // degree °
            
            // Remove any remaining non-ASCII characters that Helvetica can't handle
            content = content.replaceAll("[^\\x00-\\x7F]", "");

            String[] lines = content.split("\n");

            // Calculate pages needed (approx 45 lines per page with 12pt font)
            int linesPerPage = 45;
            int currentLine = 0;

            while (currentLine < lines.length) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
                document.addPage(page);

                org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = 
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);

                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);

                int linesOnThisPage = 0;
                while (currentLine < lines.length && linesOnThisPage < linesPerPage) {
                    String line = lines[currentLine];

                    // Wrap long lines
                    if (line.length() > 90) {
                        String[] wrappedLines = wrapText(line, 90);
                        for (String wrappedLine : wrappedLines) {
                            if (linesOnThisPage >= linesPerPage) break;
                            contentStream.showText(wrappedLine);
                            contentStream.newLineAtOffset(0, -15);
                            linesOnThisPage++;
                        }
                    } else {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -15);
                        linesOnThisPage++;
                    }
                    currentLine++;
                }

                contentStream.endText();
                contentStream.close();
            }

            // Handle empty content case
            if (lines.length == 0 || (lines.length == 1 && lines[0].isEmpty())) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
                document.addPage(page);
            }

            // Save to the original file path (overwrite)
            document.save(new File(filePath));
            document.close();

            System.out.println("✓ PDF updated: " + filePath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to wrap text at a specified width
    private String[] wrapText(String text, int width) {
        if (text.length() <= width) {
            return new String[]{text};
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + width, text.length());

            // Try to break at a space if possible
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            lines.add(text.substring(start, end).trim());
            start = end + 1;
        }

        return lines.toArray(new String[0]);
    }

    //Saves edited content to an existing DOCX file (overwrites)
    private boolean saveEditedDocxFile(String filePath, String content) {
        try {
            // Create DOCX with the edited content
            org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument();

            // Split content into lines and add as paragraphs
            String[] lines = content.split("\n");
            for (String line : lines) {
                org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
                org.apache.poi.xwpf.usermodel.XWPFRun run = paragraph.createRun();
                run.setText(line);
                run.setFontSize(11);
            }

            // Write to the original file path (overwrite)
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(filePath)) {
                document.write(out);
            }
            document.close();

            System.out.println("✓ DOCX updated: " + filePath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Gets the file extension from a path
    private String getFileExtension(String path) {
        if (path == null) return "";
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    private void handleExportPdf(Resume resume) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Resume as PDF");
        chooser.setSelectedFile(new File(exportService.getSuggestedFileName(resume, "pdf")));
        
        int result = chooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();
            String path = outputFile.getAbsolutePath();
            
            // Ensure .pdf extension
            if (!path.toLowerCase().endsWith(".pdf")) {
                path += ".pdf";
            }
            
            // Check if file exists and confirm overwrite
            File checkFile = new File(path);
            if (checkFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        view,
                        "File already exists. Do you want to overwrite it?\n" + path,
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (overwrite != JOptionPane.YES_OPTION) {
                    return; // User cancelled
                }
                // Delete existing file to allow overwrite
                checkFile.delete();
            }
            
            boolean success = exportService.exportToPDF(resume, path);
            if (success) {
                JOptionPane.showMessageDialog(
                        view,
                        "Resume exported successfully to:\n" + path,
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        view,
                        "Failed to export resume to PDF.",
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void handleExportDocx(Resume resume) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Resume as DOCX");
        chooser.setSelectedFile(new File(exportService.getSuggestedFileName(resume, "docx")));
        
        int result = chooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();
            String path = outputFile.getAbsolutePath();
            
            // Ensure .docx extension
            if (!path.toLowerCase().endsWith(".docx")) {
                path += ".docx";
            }
            
            // Check if file exists and confirm overwrite
            File checkFile = new File(path);
            if (checkFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        view,
                        "File already exists. Do you want to overwrite it?\n" + path,
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (overwrite != JOptionPane.YES_OPTION) {
                    return; // User cancelled
                }
                // Delete existing file to allow overwrite
                checkFile.delete();
            }
            
            boolean success = exportService.exportToDOCX(resume, path);
            if (success) {
                JOptionPane.showMessageDialog(
                        view,
                        "Resume exported successfully to:\n" + path,
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        view,
                        "Failed to export resume to DOCX.",
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
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

    //Copies file into an "uploads" folder and inserts a row in `resumes`.
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
