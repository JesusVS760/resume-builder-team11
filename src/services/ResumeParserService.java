/*
 * DISCLAIMER:
 * Portions of this project were supported by the use of Claude Sonnet 4.5.
 * The tool was used strictly for assistance with formatting, answering
 * technical questions, and occasional guidance during implementation.
 * ALL CONTENT WAS THOROUGHLY READ AND LEARNED THROUGH THE PROCESS
 */

package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Fully rebuilt ResumeParserService (Option C: Experience + Projects merged)
 */
public class ResumeParserService {

    // ---------------------------------------------------------
    // Parsed Resume Object
    // ---------------------------------------------------------
    public static class ParsedResume {
        private String fullText;
        private Map<String, String> sections = new HashMap<>();
        private List<String> experiences = new ArrayList<>();

        public String getFullText() { return fullText; }
        public Map<String, String> getSections() { return sections; }
        public List<String> getExperiences() { return experiences; }

        public void setFullText(String fullText) { this.fullText = fullText; }
        public void setSections(Map<String, String> sections) { this.sections = sections; }
        public void setExperiences(List<String> experiences) { this.experiences = experiences; }
    }

    // ---------------------------------------------------------
    // Public main API
    // ---------------------------------------------------------
    public ParsedResume parseResumeComplete(File file) throws IOException {
        validateFile(file);

        String rawText = extractText(file);
        rawText = normalizeText(rawText);

        ParsedResume parsed = new ParsedResume();
        parsed.setFullText(rawText);

        Map<String, String> sections = extractSections(rawText);
        parsed.setSections(sections);

        // Experience splitting (Option C: Combined experience/projects)
        List<String> experiences = splitExperienceBlocks(
                sections.getOrDefault("experience", "")
        );
        parsed.setExperiences(experiences);

        return parsed;
    }

    // ---------------------------------------------------------
    // Validation
    // ---------------------------------------------------------
    private void validateFile(File file) {
        if (!file.exists())
            throw new IllegalArgumentException("File does not exist");

        String name = file.getName().toLowerCase();
        if (!name.endsWith(".pdf") && !name.endsWith(".docx") && !name.endsWith(".doc"))
            throw new IllegalArgumentException("Invalid file type (.pdf, .docx, .doc only)");

        if (file.length() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("File too large (max 5MB)");
    }

    // ---------------------------------------------------------
    // Extract Text (PUBLIC)
    // ---------------------------------------------------------
    public String extractText(File file) throws IOException {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".pdf")) return extractPDF(file);
        if (name.endsWith(".docx")) return extractDOCX(file);
        if (name.endsWith(".doc")) return extractDOC(file);

        return "";
    }

    private String extractPDF(File file) throws IOException {
        try (PDDocument doc = PDDocument.load(file)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String extractDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(
                    p -> sb.append(p.getText()).append("\n")
            );
            return sb.toString();
        }
    }

    private String extractDOC(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {

            StringBuilder sb = new StringBuilder();
            for (String p : extractor.getParagraphText()) {
                sb.append(p.trim()).append("\n");
            }
            return sb.toString();
        }
    }

    // ---------------------------------------------------------
    // Normalization
    // ---------------------------------------------------------
    private String normalizeText(String text) {
        return text
                .replace("\r", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .replace("▪", "-")
                .replace("•", "-")
                .replace("●", "-")
                .replace("■", "-")
                .replace("–", "-")
                .replace("—", "-")
                .trim();
    }

    // ---------------------------------------------------------
    // Extract Sections
    // ---------------------------------------------------------
    private Map<String, String> extractSections(String text) {
        Map<String, String> sections = new LinkedHashMap<>();

        // Normalize combined headers BEFORE regex
        text = text.replaceAll("(?i)skills\\s*[/&]\\s*interests", "skills");
        text = text.replaceAll("(?i)experience\\s*[/&]\\s*projects", "experience");

        // Supported headers
        String[] headers = {
                "experience", "work experience", "professional experience",
                "projects",
                "skills", "technical skills",
                "education",
                "certifications", "awards", "honors",
                "languages",
                "interests"
        };

        // Build regex to detect headers
        String headerRegex = "(?im)^(" + String.join("|", headers) + ")\\s*:?$";

        Pattern pattern = Pattern.compile(headerRegex);
        Matcher matcher = pattern.matcher(text);

        List<Integer> indices = new ArrayList<>();
        List<String> names = new ArrayList<>();

        while (matcher.find()) {
            indices.add(matcher.start());
            names.add(matcher.group(1).trim().toLowerCase());
        }

        // end of document
        indices.add(text.length());

        // Build section blocks
        for (int i = 0; i < names.size(); i++) {
            int start = indices.get(i);
            int end = indices.get(i + 1);

            String header = names.get(i);
            String content = text.substring(start, end)
                    .replaceFirst("(?i)^" + Pattern.quote(header) + "\\s*:?", "")
                    .trim();

            sections.put(header, content);
        }

        // -----------------------------------------------------
        // Auto-detect Education if missing
        // -----------------------------------------------------
        if (!sections.containsKey("education")) {
            Pattern edu = Pattern.compile(
                    "(?i)([A-Z][A-Za-z0-9 .,&-]+University[\\s\\S]{30,400})"
            );
            Matcher m = edu.matcher(text);
            if (m.find()) {
                sections.put("education", m.group(1).trim());
            }
        }

        // -----------------------------------------------------
        // Fallback SKILL extraction if no SKILLS section present
        // -----------------------------------------------------
        if (!sections.containsKey("skills")) {
            Pattern skillPattern = Pattern.compile(
                    "(?i)(skills?)[:\\s\\n-]+([\\s\\S]{20,200})(?=\\n[A-Z][A-Za-z ]+:|$)"
            );
            Matcher sm = skillPattern.matcher(text);
            if (sm.find()) {
                sections.put("skills", cleanSkills(sm.group(2)));
            }
        } else {
            sections.put("skills", cleanSkills(sections.get("skills")));
        }

        return sections;
    }

    // ---------------------------------------------------------
    // Clean Skills Block
    // ---------------------------------------------------------
    private String cleanSkills(String block) {
        block = block
                .replaceAll("(?i)interests?:.*", "")
                .replaceAll("(?i)languages?:.*", "")
                .trim();

        String[] parts = block.split("[-,\\n]+");

        List<String> cleaned = new ArrayList<>();
        for (String p : parts) {
            p = p.trim();
            if (p.length() > 1) cleaned.add(p);
        }

        return String.join(", ", cleaned);
    }

    // ---------------------------------------------------------
    // Split Experience Blocks
    // ---------------------------------------------------------
    private List<String> splitExperienceBlocks(String text) {
        List<String> blocks = new ArrayList<>();

        if (text == null || text.isEmpty()) return blocks;

        // Split on lines starting with capitalized titles
        String[] candidates = text.split("(?m)^(?=[A-Z][A-Za-z0-9 ]{3,})");

        for (String c : candidates) {
            c = c.trim();
            if (c.length() > 60) {
                blocks.add(c);
            }
        }

        return blocks;
    }
}
