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
            
            // For PDF/DOC files, create a new DOCX with extracted text
            String content = readFileContent(resume.getFilePath());
            
            // Remove control characters
            content = content.replace("\r", "");
            
            XWPFDocument document = new XWPFDocument();
            FileOutputStream out = new FileOutputStream(outputPath);

            // Add title
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(resume.getFileName() != null ? resume.getFileName() : "Resume");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // Add spacing
            document.createParagraph();

            // Add content
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
