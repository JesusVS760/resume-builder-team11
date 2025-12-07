// AI prompted on what was needed to export files in different formats
package services;

import models.Resume;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Service for exporting resumes to various formats (PDF, DOCX)
 */
public class ExportService {

    /**
     * Exports a resume to PDF format
     * @param resume The resume to export
     * @param outputPath The path where the PDF should be saved
     * @return true if export successful, false otherwise
     */
    public boolean exportToPDF(Resume resume, String outputPath) {
        try {
            // Check if file already exists and prompt for overwrite
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                System.err.println("File already exists: " + outputPath);
                return false; // Let controller handle the confirmation
            }

            // For PDF export, simply copy the original file if it's already a PDF
            File sourceFile = new File(resume.getFilePath());
            String sourceName = sourceFile.getName().toLowerCase();

            if (sourceName.endsWith(".pdf")) {
                // Direct copy for PDF files - preserves exact formatting
                Files.copy(sourceFile.toPath(), Paths.get(outputPath),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✓ PDF exported successfully (copied): " + outputPath);
                return true;
            }

            // For DOCX/DOC files, create a new PDF with extracted text
            String content = readFileContent(resume.getFilePath());

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set up font and starting position
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);

            // Split content into lines and write to PDF
            if (content != null && !content.isEmpty()) {
                // Remove carriage returns and other control characters
                content = content.replace("\r", "").replace("\t", "    ");
                
                // Sanitize content - replace special Unicode characters not supported by Helvetica
                content = sanitizeForPdf(content);

                String[] lines = content.split("\n");
                for (String line : lines) {
                    // Skip empty lines
                    if (line.trim().isEmpty()) {
                        contentStream.newLineAtOffset(0, -15);
                        continue;
                    }

                    // Remove any remaining control characters
                    line = line.replaceAll("[\\p{Cntrl}&&[^\n]]", "");

                    // Handle long lines by wrapping
                    if (line.length() > 80) {
                        String[] wrappedLines = wrapText(line, 80);
                        for (String wrappedLine : wrappedLines) {
                            contentStream.showText(wrappedLine);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    } else {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -15);
                    }
                }
            }

            contentStream.endText();
            contentStream.close();

            // Save the document
            document.save(outputPath);
            document.close();

            System.out.println("✓ PDF exported successfully: " + outputPath);
            return true;

        } catch (IOException e) {
            System.err.println("Error exporting to PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exports a resume to DOCX format
     * @param resume The resume to export
     * @param outputPath The path where the DOCX should be saved
     * @return true if export successful, false otherwise
     */
    public boolean exportToDOCX(Resume resume, String outputPath) {
        try {
            // Check if file already exists
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                System.err.println("File already exists: " + outputPath);
                return false; // Let controller handle the confirmation
            }

            // For DOCX export, simply copy the original file if it's already a DOCX
            File sourceFile = new File(resume.getFilePath());
            String sourceName = sourceFile.getName().toLowerCase();

            if (sourceName.endsWith(".docx") || sourceName.endsWith(".doc")) {
                // Direct copy for DOCX/DOC files - preserves exact formatting
                Files.copy(sourceFile.toPath(), Paths.get(outputPath),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✓ DOCX exported successfully (copied): " + outputPath);
                return true;
            }

            // For PDF files, create a new DOCX with extracted text
            String content = readFileContent(resume.getFilePath());

            // Remove control characters
            content = content.replace("\r", "");

            XWPFDocument document = new XWPFDocument();
            FileOutputStream out = new FileOutputStream(outputPath);

            // Add content directly (no title)
            if (content != null && !content.isEmpty()) {
                String[] lines = content.split("\n");
                for (String line : lines) {
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(line);
                    run.setFontSize(11);
                }
            }

            // Write to file
            document.write(out);
            out.close();
            document.close();

            System.out.println("✓ DOCX exported successfully: " + outputPath);
            return true;

        } catch (NoClassDefFoundError e) {
            System.err.println("Missing Log4j dependency for DOCX creation. Falling back to simple text export.");
            // Fallback: export as text file with .docx extension
            return exportAsPlainText(resume, outputPath);
        } catch (IOException e) {
            System.err.println("Error exporting to DOCX: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fallback method to export as plain text when DOCX libraries fail
     */
    private boolean exportAsPlainText(Resume resume, String outputPath) {
        try {
            String content = readFileContent(resume.getFilePath());
            content = content.replace("\r", "");
            Files.write(Paths.get(outputPath), content.getBytes());
            System.out.println("✓ Exported as plain text: " + outputPath);
            return true;
        } catch (IOException e) {
            System.err.println("Error in fallback export: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates a preview of the resume content
     * @param resume The resume to preview
     * @return HTML formatted preview string
     */
    public String generatePreview(Resume resume) {
        if (resume == null || resume.getFilePath() == null) {
            return "<html><body><p>No content available</p></body></html>";
        }

        try {
            String content = readFileContent(resume.getFilePath());

            StringBuilder html = new StringBuilder();
            html.append("<html><head><style>");
            html.append("body { font-family: Arial, sans-serif; padding: 20px; }");
            html.append("h1 { color: #1f2937; }");
            html.append("p { line-height: 1.6; }");
            html.append("</style></head><body>");
            html.append("<h1>").append(resume.getFileName() != null ? resume.getFileName() : "Resume").append("</h1>");
            html.append("<p>").append(content.replace("\n", "<br>")).append("</p>");
            html.append("</body></html>");

            return html.toString();
        } catch (IOException e) {
            return "<html><body><p>Error reading file: " + e.getMessage() + "</p></body></html>";
        }
    }

    /**
     * Reads file content from the file path
     */
    private String readFileContent(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        // Use ResumeParserService to extract text from PDF/DOCX
        ResumeParserService parser = new ResumeParserService();
        try {
            return parser.extractText(file);
        } catch (Exception e) {
            // Fallback: try to read as plain text
            return new String(Files.readAllBytes(Paths.get(filePath)));
        }
    }

    /**
     * Sanitizes text content for PDF export by replacing/removing characters
     * not supported by the Helvetica font
     */
    private String sanitizeForPdf(String content) {
        if (content == null) return "";
        
        // Replace box-drawing characters
        content = content.replace("\u2550", "=");  // ═ double horizontal
        content = content.replace("\u2551", "|");  // ║ double vertical
        content = content.replace("\u2554", "+");  // ╔ double down and right
        content = content.replace("\u2557", "+");  // ╗ double down and left
        content = content.replace("\u255A", "+");  // ╚ double up and right
        content = content.replace("\u255D", "+");  // ╝ double up and left
        content = content.replace("\u2560", "+");  // ╠ double vertical and right
        content = content.replace("\u2563", "+");  // ╣ double vertical and left
        content = content.replace("\u2566", "+");  // ╦ double down and horizontal
        content = content.replace("\u2569", "+");  // ╩ double up and horizontal
        content = content.replace("\u256C", "+");  // ╬ double vertical and horizontal
        content = content.replace("\u2500", "-");  // ─ light horizontal
        content = content.replace("\u2502", "|");  // │ light vertical
        
        // Replace bullet points and squares
        content = content.replace("\u25A0", "*");  // ■ black square
        content = content.replace("\u25AA", "*");  // ▪ small black square
        content = content.replace("\u25CF", "*");  // ● black circle
        content = content.replace("\u2022", "*");  // • bullet
        content = content.replace("\u25E6", "o");  // ◦ white bullet
        content = content.replace("\u25B8", ">");  // ▸ right arrow
        content = content.replace("\u25BA", ">");  // ► right pointer
        content = content.replace("\u25C6", "*");  // ◆ black diamond
        content = content.replace("\u2756", "*");  // ❖ diamond minus
        
        // Replace arrows
        content = content.replace("\u2192", "->");  // → right arrow
        content = content.replace("\u2190", "<-");  // ← left arrow
        content = content.replace("\u2191", "^");   // ↑ up arrow
        content = content.replace("\u2193", "v");   // ↓ down arrow
        
        // Replace dashes and quotes
        content = content.replace("\u2013", "-");   // – en dash
        content = content.replace("\u2014", "--");  // — em dash
        content = content.replace("\u2018", "'");   // ' left single quote
        content = content.replace("\u2019", "'");   // ' right single quote
        content = content.replace("\u201C", "\"");  // " left double quote
        content = content.replace("\u201D", "\"");  // " right double quote
        content = content.replace("\u2026", "..."); // … ellipsis
        
        // Replace other common symbols
        content = content.replace("\u00A9", "(c)");  // © copyright
        content = content.replace("\u00AE", "(R)");  // ® registered
        content = content.replace("\u2122", "(TM)"); // ™ trademark
        content = content.replace("\u00B0", " deg"); // ° degree
        content = content.replace("\u00B7", "*");    // · middle dot
        content = content.replace("\u2023", ">");    // ‣ triangular bullet
        content = content.replace("\u2043", "-");    // ⁃ hyphen bullet
        
        // Replace checkmarks and crosses
        content = content.replace("\u2713", "[x]");  // ✓ check mark
        content = content.replace("\u2714", "[x]");  // ✔ heavy check mark
        content = content.replace("\u2715", "[X]");  // ✕ multiplication x
        content = content.replace("\u2717", "[X]");  // ✗ ballot x
        content = content.replace("\u2718", "[X]");  // ✘ heavy ballot x
        
        // Replace stars
        content = content.replace("\u2605", "*");    // ★ black star
        content = content.replace("\u2606", "*");    // ☆ white star
        
        // Remove any remaining non-ASCII characters that Helvetica can't handle
        content = content.replaceAll("[^\\x00-\\x7F]", "");
        
        return content;
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

    /**
     * Gets a suggested filename for export
     */
    public String getSuggestedFileName(Resume resume, String extension) {
        String baseName = resume.getFileName();
        if (baseName == null || baseName.isEmpty()) {
            baseName = "resume";
        } else {
            // Remove existing extension if present
            int lastDot = baseName.lastIndexOf('.');
            if (lastDot > 0) {
                baseName = baseName.substring(0, lastDot);
            }
        }
        return baseName + "_exported." + extension;
    }
}