package controllers;

import ui.UploadPanel;
import services.ResumeParserService;
import services.ResumeParserService.ParsedResume;
import services.ResumeAnalyzeService;

import dao.ResumeDAO;
import dao.AnalyzedResumeDAO;
import models.Resume;
import models.AnalyzedResume;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;

public class UploadController extends BaseController<UploadPanel> {
    private final ResumeParserService parser;
    private final ResumeAnalyzeService tailoringService;
    private final ResumeDAO resumeDAO;
    private final AnalyzedResumeDAO tailoredResumeDAO;

    private ParsedResume lastParsed;

    public UploadController(UploadPanel view,
                            ResumeParserService parser,
                            ResumeAnalyzeService tailoringService,
                            ResumeDAO resumeDAO,
                            AnalyzedResumeDAO tailoredResumeDAO) {
        super(view);
        this.parser = parser;
        this.tailoringService = tailoringService;
        this.resumeDAO = resumeDAO;
        this.tailoredResumeDAO = tailoredResumeDAO;
        attach();
    }

    private void attach() {
        // When a file is dropped/selected, just store it - don't save yet
        view.setOnFileDropped(file -> {
            // File is already stored by the view, no action needed here
            // We'll save it when the user clicks "Analyze Resume"
        });

        // Parse and save when user clicks "Analyze Resume"
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
            private int formattedResumeId = -1;
            private String tailoredText; // may stay null if no jobDesc

            @Override
            protected ParsedResume doInBackground() throws Exception {
                updateProgress(15, "Validating file…");

                // 1) Save original resume file + DB record
                resumeId = saveResumeToDatabase(file);

                updateProgress(35, "Extracting text…");
                updateProgress(60, "Parsing sections…");

                ParsedResume parsed = parser.parseResumeComplete(file);

                // 2) Generate + save formatted resume if we have a job description
                if (jobDesc != null && !jobDesc.isBlank()) {
                    updateProgress(75, "Analysis of resume to job…");

                    tailoredText = tailoringService.tailorResume(parsed, jobDesc);

                    // Only persist if we successfully saved the original resume
                    if (resumeId > 0 && tailoredText != null && !tailoredText.isBlank()) {
                        // Create formatted resume file in the same format as original
                        formattedResumeId = saveFormattedResume(file, tailoredText);
                        
                        // Also save to analyzed_resumes table
                        AnalyzedResume tr = new AnalyzedResume(
                                resumeId,
                                null,          // jobTitle (optional for now)
                                null,          // jobCompany
                                jobDesc,
                                tailoredText,
                                null           // filePath if you later export to PDF/DOCX
                        );

                        tailoredResumeDAO.saveTailoredResume(tr);
                    }
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

                    // Existing summary popup with parsed sections/summary
//                    showParsedSummary(lastParsed, file, jobDesc);

                    // If we actually tailored something, let the view show the two-panel dialog
                    if (tailoredText != null && !tailoredText.isBlank()) {
                        view.showTailoringResult(tailoredText);

                        // And let the user know what happened with saving
                        if (resumeId > 0 && formattedResumeId > 0) {
                            JOptionPane.showMessageDialog(
                                    view,
                                    "Your original resume and analyzed resume have been saved.\n" +
                                            "Check 'Saved Resumes' to view them.",
                                    "Resumes Saved",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else if (resumeId > 0) {
                            JOptionPane.showMessageDialog(
                                    view,
                                    "Your original resume has been saved.\n" +
                                            "The analyzed version could not be created.",
                                    "Resume Saved",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    view,
                                    "A resume analysis was generated, but you are not logged in,\n" +
                                            "so it was not saved to your account.",
                                    "Analysis Generated",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
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
     * returning the new resume's database id. If no user is logged in, it returns -1
     * and does not persist anything.
     */
    private int saveResumeToDatabase(File originalFile) throws IOException, SQLException {
        String userId = getCurrentUserId();

        // If nobody is logged in, we skip saving but still allow parsing.
        if (userId == null || userId.isBlank()) {
            return -1;
        }

        Path uploadsDir = Paths.get("uploads");
        if (Files.notExists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        String storedFileName = userId + "_" + System.currentTimeMillis() + "_" + originalFile.getName();
        Path dest = uploadsDir.resolve(storedFileName);

        Files.copy(originalFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        // Resume model should use String userId
        Resume resume = new Resume(userId, originalFile.getName(), dest.toString());
        return resumeDAO.saveResume(resume);
    }

    /**
     * Creates an analyzed resume file with "analyzed_" prefix in the same format as the original,
     * and saves it to the database.
     */
    private int saveFormattedResume(File originalFile, String formattedContent) throws IOException, SQLException {
        String userId = getCurrentUserId();

        if (userId == null || userId.isBlank()) {
            return -1;
        }

        Path uploadsDir = Paths.get("uploads");
        if (Files.notExists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        // Determine the file extension from the original file
        String originalName = originalFile.getName();
        String extension = getFileExtension(originalName);
        String baseName = getFileBaseName(originalName);
        
        // Create analyzed filename: analyzed_<originalname>.<ext>
        String formattedFileName = "analyzed_" + baseName + "." + extension;
        String storedFileName = userId + "_" + System.currentTimeMillis() + "_" + formattedFileName;
        Path dest = uploadsDir.resolve(storedFileName);

        // Clean the content - extract only the resume part (remove markers)
        String cleanedContent = extractResumeContent(formattedContent);

        // Create the formatted file in the same format as original
        boolean success;
        if ("pdf".equalsIgnoreCase(extension)) {
            success = createFormattedPdf(dest.toString(), cleanedContent);
        } else {
            // Default to DOCX for docx, doc, or any other format
            success = createFormattedDocx(dest.toString(), cleanedContent);
        }

        if (!success) {
            return -1;
        }

        // Save to database
        Resume resume = new Resume(userId, formattedFileName, dest.toString());
        return resumeDAO.saveResume(resume);
    }

    /**
     * Cleans the analyzed content by removing marker tags but keeping all content
     * (both feedback and resume).
     */
    private String extractResumeContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        // Remove all marker tags but keep the content
        return content
                .replace("===FEEDBACK_START===", "")
                .replace("===FEEDBACK_END===", "")
                .replace("===RESUME_START===", "")
                .replace("===RESUME_END===", "")
                .trim();
    }

    /**
     * Creates a PDF file with the formatted content
     */
    private boolean createFormattedPdf(String filePath, String content) {
        try {
            org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();

            // Clean content - remove control characters and replace special Unicode
            content = content.replace("\r", "").replace("\t", "    ");
            content = content.replaceAll("[\\p{Cntrl}&&[^\n]]", "");
            
            // Replace common special characters with ASCII equivalents
            content = content.replace("\u25A0", "-").replace("\u25AA", "-");
            content = content.replace("\u25CF", "-").replace("\u2022", "-");
            content = content.replace("\u25E6", "-").replace("\u25B8", "-");
            content = content.replace("\u25BA", "-").replace("\u2192", "->");
            content = content.replace("\u2190", "<-").replace("\u2013", "-");
            content = content.replace("\u2014", "-").replace("\u2018", "'");
            content = content.replace("\u2019", "'").replace("\u201C", "\"");
            content = content.replace("\u201D", "\"").replace("\u2026", "...");
            content = content.replace("\u00A9", "(c)").replace("\u00AE", "(R)");
            content = content.replace("\u2122", "(TM)").replace("\u00B0", " deg");
            content = content.replaceAll("[^\\x00-\\x7F]", "");

            String[] lines = content.split("\n");
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

            if (lines.length == 0 || (lines.length == 1 && lines[0].isEmpty())) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
                document.addPage(page);
            }

            document.save(new File(filePath));
            document.close();

            System.out.println("✓ Analyzed PDF created: " + filePath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a DOCX file with the formatted content
     */
    private boolean createFormattedDocx(String filePath, String content) {
        try {
            org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument();

            String[] lines = content.split("\n");
            for (String line : lines) {
                org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
                org.apache.poi.xwpf.usermodel.XWPFRun run = paragraph.createRun();
                run.setText(line);
                run.setFontSize(11);
            }

            try (java.io.FileOutputStream out = new java.io.FileOutputStream(filePath)) {
                document.write(out);
            }
            document.close();

            System.out.println("✓ Analyzed DOCX created: " + filePath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to wrap text at a specified width
     */
    private String[] wrapText(String text, int width) {
        if (text.length() <= width) {
            return new String[]{text};
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + width, text.length());

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

    /**
     * Gets the file extension from a filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) return "docx";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "docx"; // default
    }

    /**
     * Gets the base name (without extension) from a filename
     */
    private String getFileBaseName(String fileName) {
        if (fileName == null) return "resume";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }


    public ParsedResume getLastParsed() {
        return lastParsed;
    }

}
