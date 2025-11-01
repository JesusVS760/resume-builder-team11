package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ResumeParserService {  // Fixed typo: "Service" -> "Service"

    public String parseResume(File file) throws IOException {
        validateFile(file);
        return extractText(file);  // Fixed method name
    }

    public void validateFile(File file) {  // Fixed method name
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        String fileName = file.getName().toLowerCase();
        // Fixed logic: should be && (AND) not || (OR)
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".docx") && !fileName.endsWith(".doc")) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, DOC, and DOCX are allowed");
        }

        long maxSizeBytes = 5 * 1024 * 1024; // 5MB
        if (file.length() > maxSizeBytes) {
            throw new IllegalArgumentException("File is too large. Max size is 5MB.");
        }
    }

    private String extractText(File file) throws IOException {  // Fixed method name
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractTextFromPDF(file);
        } else if (fileName.endsWith(".docx")) {
            return extractTextFromDOCX(file);
        } else if (fileName.endsWith(".doc")) {
            return extractTextFromDOC(file);
        }
        throw new IllegalArgumentException("Unsupported file format.");
    }

    private String extractTextFromPDF(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFromDOC(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}