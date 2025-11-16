package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ResumeParserService {

    /**
     * Represents a parsed resume with all sections.
     */
    public static class ParsedResume {
        private String fullText;
        private Map<String, String> sections;
        private List<String> experiences;

        public ParsedResume() {
            this.sections = new HashMap<>();
            this.experiences = new ArrayList<>();
        }

        public String getFullText() { return fullText; }
        public void setFullText(String fullText) { this.fullText = fullText; }

        public Map<String, String> getSections() { return sections; }
        public void setSections(Map<String, String> sections) { this.sections = sections; }

        public List<String> getExperiences() { return experiences; }
        public void setExperiences(List<String> experiences) { this.experiences = experiences; }

        public String getSection(String sectionName) {
            return sections.getOrDefault(sectionName.toLowerCase(), "");
        }
    }

    /**
     * Parses a resume and returns all text.
     */
    public String parseResume(File file) throws IOException {
        validateFile(file);
        return extractText(file);
    }

    /**
     * Parses a resume and extracts all sections including experience, skills, education, etc.
     */
    public ParsedResume parseResumeComplete(File file) throws IOException {
        String resumeText = parseResume(file);
        ParsedResume parsed = new ParsedResume();
        parsed.setFullText(resumeText);

        // Extract all sections
        Map<String, String> sections = extractAllSections(resumeText);
        parsed.setSections(sections);

        // Extract experience as list of individual job entries
        List<String> experiences = parseIndividualExperiences(
                sections.getOrDefault("experience", "")
        );
        parsed.setExperiences(experiences);

        return parsed;
    }

    /**
     * Parses a resume and extracts all experience blocks.
     * @deprecated Use parseResumeComplete() for full resume parsing
     */
    @Deprecated
    public List<String> parseExperiences(File file) throws IOException {
        String resumeText = parseResume(file);
        return extractExperienceBlocks(resumeText);
    }

    /**
     * Validates the file type and size.
     */
    private void validateFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".docx") && !fileName.endsWith(".doc")) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, DOC, and DOCX are allowed");
        }

        long maxSizeBytes = 5 * 1024 * 1024; // 5MB
        if (file.length() > maxSizeBytes) {
            throw new IllegalArgumentException("File is too large. Max size is 5MB.");
        }
    }

    /**
     * Extracts text based on file type.
     */
    public String extractText(File file) throws IOException {
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

            // Keep paragraph separation for better parsing
            StringBuilder sb = new StringBuilder();
            document.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n\n"));
            return sb.toString();
        }
    }

    private String extractTextFromDOC(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {

            // WordExtractor.getParagraphText() keeps paragraphs separated
            StringBuilder sb = new StringBuilder();
            for (String para : extractor.getParagraphText()) {
                sb.append(para).append("\n\n");
            }
            return sb.toString();
        }
    }

    /**
     * Extracts all major sections from resume text.
     * Looks for common section headers and captures content until the next section.
     */
    private Map<String, String> extractAllSections(String text) {
        Map<String, String> sections = new HashMap<>();

        // Common section headers in resumes
        String[] sectionHeaders = {
                "Experience", "Work Experience", "Professional Experience", "Employment History",
                "Education", "Academic Background",
                "Skills", "Technical Skills", "Core Competencies", "Proficiencies",
                "Projects", "Key Projects",
                "Certifications", "Certificates", "Professional Certifications",
                "Awards", "Honors", "Achievements",
                "Volunteer", "Volunteer Experience", "Community Service",
                "Interests", "Hobbies", "Personal Interests",
                "Publications", "Research",
                "Languages", "Language Skills",
                "Summary", "Professional Summary", "Profile", "Objective"
        };

        // Build regex pattern to match any section header
        String headerPattern = String.join("|", sectionHeaders);

        // Pattern to find section headers and capture content until next header
        Pattern pattern = Pattern.compile(
                "(?i)^\\s*(" + headerPattern + ")\\s*:?\\s*$([\\s\\S]*?)(?=^\\s*(?:" + headerPattern + ")\\s*:?\\s*$|\\z)",
                Pattern.MULTILINE
        );

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String sectionName = matcher.group(1).trim().toLowerCase();
            String sectionContent = matcher.group(2).trim();

            if (!sectionContent.isEmpty()) {
                // Normalize section names
                String normalizedName = normalizeSectionName(sectionName);

                // If section already exists, append to it (handles multiple matches)
                if (sections.containsKey(normalizedName)) {
                    sections.put(normalizedName,
                            sections.get(normalizedName) + "\n\n" + sectionContent);
                } else {
                    sections.put(normalizedName, sectionContent);
                }
            }
        }

        return sections;
    }

    /**
     * Normalizes section names to standard keys.
     */
    private String normalizeSectionName(String sectionName) {
        sectionName = sectionName.toLowerCase().trim();

        // Map variations to standard names
        if (sectionName.contains("experience") || sectionName.contains("employment")) {
            return "experience";
        } else if (sectionName.contains("education") || sectionName.contains("academic")) {
            return "education";
        } else if (sectionName.contains("skill") || sectionName.contains("competenc") ||
                sectionName.contains("proficienc")) {
            return "skills";
        } else if (sectionName.contains("project")) {
            return "projects";
        } else if (sectionName.contains("certif")) {
            return "certifications";
        } else if (sectionName.contains("award") || sectionName.contains("honor") ||
                sectionName.contains("achievement")) {
            return "awards";
        } else if (sectionName.contains("volunteer") || sectionName.contains("community")) {
            return "volunteer";
        } else if (sectionName.contains("interest") || sectionName.contains("hobbies")) {
            return "interests";
        } else if (sectionName.contains("publication") || sectionName.contains("research")) {
            return "publications";
        } else if (sectionName.contains("language")) {
            return "languages";
        } else if (sectionName.contains("summary") || sectionName.contains("profile") ||
                sectionName.contains("objective")) {
            return "summary";
        }

        return sectionName;
    }

    /**
     * Parses individual job experiences from the experience section.
     */
    private List<String> parseIndividualExperiences(String experienceText) {
        List<String> experiences = new ArrayList<>();

        if (experienceText == null || experienceText.isEmpty()) {
            return experiences;
        }

        // Pattern to match company/position entries
        // Looks for patterns like: "Company Name" or "Position Title"
        // followed by location and dates
        Pattern jobPattern = Pattern.compile(
                "(?m)^([A-Z][^\\n]{10,80})\\s*$\\s*([^\\n]+?)\\s*[|–—-]?\\s*([^\\n]*(?:20\\d{2}|Present)[^\\n]*)\\s*$([\\s\\S]*?)(?=^[A-Z][^\\n]{10,80}\\s*$|\\z)",
                Pattern.MULTILINE
        );

        Matcher matcher = jobPattern.matcher(experienceText);
        while (matcher.find()) {
            String jobBlock = matcher.group(0).trim();
            if (!jobBlock.isEmpty() && jobBlock.length() > 50) { // Reasonable minimum length
                experiences.add(jobBlock);
            }
        }

        // Fallback: if no matches, try splitting by double line breaks
        if (experiences.isEmpty()) {
            String[] blocks = experienceText.split("\n\n\n+");
            for (String block : blocks) {
                block = block.trim();
                if (!block.isEmpty() && block.length() > 50) {
                    experiences.add(block);
                }
            }
        }

        return experiences;
    }

    /**
     * Old method - extracts experience blocks from resume text.
     * @deprecated Use parseResumeComplete() instead
     */
    @Deprecated
    private List<String> extractExperienceBlocks(String text) {
        List<String> experiences = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(?i)(Experience|Work Experience)[:\\s]*([\\s\\S]*?)(Education|Skills|$)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String expBlock = matcher.group(2).trim();
            if (!expBlock.isEmpty()) {
                experiences.add(expBlock);
            }
        }

        return experiences;
    }
}