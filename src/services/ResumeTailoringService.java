package services;

import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.*;

public class ResumeTailoringService {

    private static final Set<String> STOPWORDS = Set.of(
            "and", "or", "with", "the", "a", "an", "to", "of", "in", "for",
            "on", "at", "by", "is", "are", "as", "be", "this", "that", "will",
            "we", "our", "you", "your", "from"
    );

    /**
     * Extracts and ranks important keywords from a job description
     */
    public List<String> analyzeJobDescription(String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) {
            return Collections.emptyList();
        }

        String[] tokens = jobDescription.toLowerCase().split("[^a-zA-Z0-9+]+");
        Map<String, Long> frequency = Arrays.stream(tokens)
                .filter(token -> token.length() > 2 && !STOPWORDS.contains(token))
                .collect(Collectors.groupingBy(token -> token, Collectors.counting()));

        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Matches extracted job keywords against resume text
     */
    public List<String> mapKeywords(String resumeText, List<String> jobKeywords) {
        if (resumeText == null || jobKeywords == null) {
            return Collections.emptyList();
        }

        String lowerResume = resumeText.toLowerCase();
        return jobKeywords.stream()
                .filter(lowerResume::contains)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Generates a complete, professionally formatted tailored resume.
     *
     * @param parsedResume Parsed resume object from {@link ResumeParserService}.
     * @param jobDescription Full text of the job posting.
     * @return Complete tailored resume document.
     */
    public String tailorResume(ResumeParserService.ParsedResume parsedResume, String jobDescription) {
        if (parsedResume == null || parsedResume.getFullText() == null) {
            return "Error: Resume is empty or invalid.";
        }

        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return formatProfessionalResume(parsedResume, Collections.emptyList());
        }

        String originalResume = parsedResume.getFullText();
        List<String> jobKeywords = analyzeJobDescription(jobDescription);
        List<String> matchedKeywords = mapKeywords(originalResume, jobKeywords);

        return formatProfessionalResume(parsedResume, matchedKeywords);
    }

    /**
     * Formats the resume in a professional layout with proper bullet points and sections
     */
    private String formatProfessionalResume(ResumeParserService.ParsedResume parsedResume,
                                            List<String> matchedKeywords) {
        StringBuilder resume = new StringBuilder();
        Map<String, String> sections = parsedResume.getSections();

        // 1. HEADER - Extract name and contact info
        String header = extractHeader(parsedResume.getFullText());
        if (header != null && !header.trim().isEmpty()) {
            resume.append(header);
            resume.append("\n").append(repeatChar('═', 80)).append("\n\n");
        }

        // 2. PROFESSIONAL SUMMARY
        String summary = sections.get("summary");
        if (summary != null && !summary.trim().isEmpty()) {
            resume.append("PROFESSIONAL SUMMARY\n");
            resume.append(repeatChar('─', 80)).append("\n");
            String formattedSummary = formatSummary(summary, matchedKeywords);
            resume.append(formattedSummary).append("\n\n");
        } else if (!matchedKeywords.isEmpty()) {
            // Create a summary from matched keywords
            resume.append("PROFESSIONAL SUMMARY\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append("Results-driven professional with expertise in ");
            resume.append(matchedKeywords.stream()
                    .limit(8)
                    .map(this::capitalize)
                    .collect(Collectors.joining(", ")));
            resume.append(". Proven track record of delivering high-quality solutions and ");
            resume.append("driving business success through technical excellence.\n\n");
        }

        // 3. CORE COMPETENCIES / SKILLS
        String skills = sections.get("skills");
        if (skills != null && !skills.trim().isEmpty()) {
            resume.append("CORE COMPETENCIES\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatSkillsSection(skills, matchedKeywords));
            resume.append("\n");
        }

        // 4. PROFESSIONAL EXPERIENCE
        String experience = sections.get("experience");
        if (experience != null && !experience.trim().isEmpty()) {
            resume.append("PROFESSIONAL EXPERIENCE\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatExperienceSection(experience, matchedKeywords));
            resume.append("\n");
        } else if (!parsedResume.getExperiences().isEmpty()) {
            resume.append("PROFESSIONAL EXPERIENCE\n");
            resume.append(repeatChar('─', 80)).append("\n");
            for (String exp : parsedResume.getExperiences()) {
                resume.append(formatExperienceEntry(exp, matchedKeywords));
                resume.append("\n");
            }
            resume.append("\n");
        }

        // 5. EDUCATION
        String education = sections.get("education");
        if (education != null && !education.trim().isEmpty()) {
            resume.append("EDUCATION\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatEducationSection(education));
            resume.append("\n");
        }

        // 6. CERTIFICATIONS
        String certifications = sections.get("certifications");
        if (certifications != null && !certifications.trim().isEmpty()) {
            resume.append("CERTIFICATIONS\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatBulletSection(certifications));
            resume.append("\n");
        }

        // 7. PROJECTS
        String projects = sections.get("projects");
        if (projects != null && !projects.trim().isEmpty()) {
            resume.append("KEY PROJECTS\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatProjectsSection(projects, matchedKeywords));
            resume.append("\n");
        }

        // 8. ADDITIONAL SECTIONS
        String[] additionalSections = {"awards", "publications", "volunteer", "languages", "interests"};
        for (String sectionKey : additionalSections) {
            String content = sections.get(sectionKey);
            if (content != null && !content.trim().isEmpty()) {
                resume.append(sectionKey.toUpperCase()).append("\n");
                resume.append(repeatChar('─', 80)).append("\n");
                resume.append(formatBulletSection(content));
                resume.append("\n");
            }
        }

        // 9. FOOTER with Match Score
        resume.append(repeatChar('═', 80)).append("\n");
        if (!matchedKeywords.isEmpty()) {
            double matchScore = calculatedMatchScore(parsedResume.getFullText(),
                    String.join(" ", matchedKeywords)); // Approximate scoring
            resume.append(String.format("Job Match Score: %.1f%% | Keywords Matched: %d | ★ indicates strong alignment with requirements\n",
                    matchScore, matchedKeywords.size()));
        } else {
            resume.append("★ indicates strong alignment with requirements\n");
        }

        return resume.toString();
    }

    /**
     * Extracts header (name and contact info) from resume
     */
    private String extractHeader(String resumeText) {
        String[] lines = resumeText.split("\n");
        StringBuilder header = new StringBuilder();

        // First 5-10 lines usually contain header info
        int headerLines = 0;
        for (int i = 0; i < Math.min(10, lines.length) && headerLines < 6; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // Stop at first section header
            if (isSectionHeader(line)) break;

            // Center-align name (first substantial line)
            if (headerLines == 0 && line.length() > 3) {
                header.append(centerText(line.toUpperCase(), 80)).append("\n");
            } else {
                header.append(centerText(line, 80)).append("\n");
            }
            headerLines++;
        }

        return header.toString();
    }

    /**
     * Formats the professional summary with keyword enhancement
     */
    private String formatSummary(String summary, List<String> matchedKeywords) {
        // Clean up the summary
        String cleaned = summary.trim().replaceAll("\\s+", " ");

        // Wrap at 80 characters
        return wrapText(cleaned, 80);
    }

    /**
     * Formats skills section with columns and highlighting
     */
    private String formatSkillsSection(String skills, List<String> matchedKeywords) {
        StringBuilder formatted = new StringBuilder();

        // Extract individual skills
        List<String> skillList = extractSkills(skills);

        // Sort: matched skills first
        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (String skill : skillList) {
            boolean isMatched = matchedKeywords.stream()
                    .anyMatch(kw -> skill.toLowerCase().contains(kw));
            if (isMatched) {
                matched.add("★ " + skill);
            } else {
                unmatched.add("• " + skill);
            }
        }

        // Combine and display in 3 columns
        List<String> allSkills = new ArrayList<>(matched);
        allSkills.addAll(unmatched);

        for (int i = 0; i < allSkills.size(); i += 3) {
            StringBuilder row = new StringBuilder();
            for (int j = i; j < Math.min(i + 3, allSkills.size()); j++) {
                row.append(String.format("%-26s", allSkills.get(j)));
            }
            formatted.append(row.toString().trim()).append("\n");
        }

        return formatted.toString();
    }

    /**
     * Extracts individual skills from skills text
     */
    private List<String> extractSkills(String skillsText) {
        List<String> skills = new ArrayList<>();

        // Split by common delimiters
        String[] parts = skillsText.split("[,•\n|]+");

        for (String part : parts) {
            String skill = part.trim()
                    .replaceAll("^[-•*]\\s*", "")  // Remove leading bullets
                    .replaceAll("\\s+", " ");       // Normalize spaces

            if (!skill.isEmpty() && skill.length() > 2 && skill.length() < 50) {
                skills.add(skill);
            }
        }

        return skills;
    }

    /**
     * Formats the experience section with professional bullet points
     */
    private String formatExperienceSection(String experience, List<String> matchedKeywords) {
        StringBuilder formatted = new StringBuilder();

        // Split into individual job entries
        List<String> jobs = splitIntoJobs(experience);

        for (String job : jobs) {
            formatted.append(formatExperienceEntry(job, matchedKeywords));
            formatted.append("\n");
        }

        return formatted.toString();
    }

    /**
     * Formats a single job experience entry with bullet points
     */
    private String formatExperienceEntry(String jobText, List<String> matchedKeywords) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = jobText.split("\n");

        if (lines.length == 0) return "";

        // Check if this job is relevant
        boolean isRelevant = matchedKeywords.stream()
                .filter(kw -> kw.length() > 3)
                .anyMatch(kw -> jobText.toLowerCase().contains(kw));

        // First line: Job Title
        String firstLine = lines[0].trim();
        if (!firstLine.isEmpty()) {
            formatted.append(isRelevant ? "★ " : "").append(firstLine).append("\n");
        }

        // Extract company, location, dates from second/third lines
        boolean foundMetadata = false;
        for (int i = 1; i < Math.min(3, lines.length); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // Look for dates (contains year like 2020, 2021, etc.)
            if (line.matches(".*\\b20\\d{2}\\b.*") || line.toLowerCase().contains("present")) {
                formatted.append(line).append("\n");
                foundMetadata = true;
            } else if (!foundMetadata && line.length() > 5) {
                formatted.append(line).append("\n");
            }
        }

        formatted.append("\n");

        // Rest of lines: convert to bullet points
        int startIdx = foundMetadata ? 3 : 2;
        List<String> bullets = new ArrayList<>();
        StringBuilder currentBullet = new StringBuilder();

        for (int i = startIdx; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                if (currentBullet.length() > 0) {
                    bullets.add(currentBullet.toString());
                    currentBullet = new StringBuilder();
                }
                continue;
            }

            // Check if line starts with bullet or is a new point
            if (line.matches("^[•●■▪-].*") ||
                    (currentBullet.length() == 0 && line.length() > 10)) {
                if (currentBullet.length() > 0) {
                    bullets.add(currentBullet.toString());
                }
                currentBullet = new StringBuilder(line.replaceAll("^[•●■▪-]\\s*", ""));
            } else {
                // Continuation of previous bullet
                if (currentBullet.length() > 0) {
                    currentBullet.append(" ").append(line);
                } else {
                    currentBullet.append(line);
                }
            }
        }

        if (currentBullet.length() > 0) {
            bullets.add(currentBullet.toString());
        }

        // Format bullets
        for (String bullet : bullets) {
            String cleaned = bullet.trim().replaceAll("\\s+", " ");
            if (cleaned.length() > 10) {  // Minimum bullet length
                // Check if bullet contains matched keywords
                boolean bulletMatched = matchedKeywords.stream()
                        .anyMatch(kw -> cleaned.toLowerCase().contains(kw));

                String prefix = bulletMatched ? "  ★ " : "  • ";
                formatted.append(wrapBullet(prefix + cleaned, 80, prefix.length()));
                formatted.append("\n");
            }
        }

        return formatted.toString();
    }

    /**
     * Splits experience text into individual job entries
     */
    private List<String> splitIntoJobs(String experience) {
        List<String> jobs = new ArrayList<>();
        String[] lines = experience.split("\n");

        StringBuilder currentJob = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            // Detect job title (capitalized line, not too long, not a bullet)
            boolean isJobTitle = trimmed.length() > 10 &&
                    trimmed.length() < 80 &&
                    Character.isUpperCase(trimmed.charAt(0)) &&
                    !trimmed.matches("^[•●■▪-].*") &&
                    (trimmed.matches(".*(?:Manager|Engineer|Developer|Director|Analyst|Specialist|Coordinator|Lead|Senior|Junior|Intern).*") ||
                            currentJob.length() > 200);  // Start new job after enough content

            if (isJobTitle && currentJob.length() > 100) {
                jobs.add(currentJob.toString());
                currentJob = new StringBuilder();
            }

            currentJob.append(line).append("\n");
        }

        if (currentJob.length() > 0) {
            jobs.add(currentJob.toString());
        }

        return jobs.isEmpty() ? Collections.singletonList(experience) : jobs;
    }

    /**
     * Formats education section
     */
    private String formatEducationSection(String education) {
        StringBuilder formatted = new StringBuilder();
        String[] entries = education.split("\n\n+");

        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;

            String[] lines = entry.trim().split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    formatted.append(trimmed).append("\n");
                }
            }
            formatted.append("\n");
        }

        return formatted.toString();
    }

    /**
     * Formats projects section with bullet points
     */
    private String formatProjectsSection(String projects, List<String> matchedKeywords) {
        StringBuilder formatted = new StringBuilder();
        String[] entries = projects.split("\n\n+");

        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;

            boolean isRelevant = matchedKeywords.stream()
                    .anyMatch(kw -> entry.toLowerCase().contains(kw));

            String[] lines = entry.trim().split("\n");
            if (lines.length > 0) {
                // Project title
                formatted.append(isRelevant ? "★ " : "• ");
                formatted.append(lines[0].trim()).append("\n");

                // Project details
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (!line.isEmpty()) {
                        formatted.append("  ").append(line).append("\n");
                    }
                }
                formatted.append("\n");
            }
        }

        return formatted.toString();
    }

    /**
     * Formats generic section with bullet points
     */
    private String formatBulletSection(String content) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            String trimmed = line.trim().replaceAll("^[•●■▪-]\\s*", "");
            if (!trimmed.isEmpty() && trimmed.length() > 3) {
                formatted.append("• ").append(trimmed).append("\n");
            }
        }

        return formatted.toString();
    }

    /**
     * Wraps text at specified width
     */
    private String wrapText(String text, int width) {
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split("\\s+");
        int lineLength = 0;

        for (String word : words) {
            if (lineLength + word.length() + 1 > width) {
                wrapped.append("\n");
                lineLength = 0;
            }
            if (lineLength > 0) {
                wrapped.append(" ");
                lineLength++;
            }
            wrapped.append(word);
            lineLength += word.length();
        }

        return wrapped.toString();
    }

    /**
     * Wraps bullet point text with proper indentation
     */
    private String wrapBullet(String text, int width, int indent) {
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split("\\s+");
        int lineLength = 0;
        boolean firstLine = true;

        for (String word : words) {
            if (lineLength + word.length() + 1 > width) {
                wrapped.append("\n");
                if (!firstLine) {
                    wrapped.append(repeatChar(' ', indent));
                }
                lineLength = firstLine ? 0 : indent;
                firstLine = false;
            }
            if (lineLength > 0) {
                wrapped.append(" ");
                lineLength++;
            }
            wrapped.append(word);
            lineLength += word.length();
        }

        return wrapped.toString();
    }

    /**
     * Centers text within specified width
     */
    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return repeatChar(' ', padding) + text;
    }

    /**
     * Checks if a line is a section header
     */
    private boolean isSectionHeader(String line) {
        if (line.length() < 3 || line.length() > 50) return false;

        String[] sections = {
                "SUMMARY", "OBJECTIVE", "PROFILE", "EXPERIENCE", "WORK EXPERIENCE",
                "EMPLOYMENT", "SKILLS", "TECHNICAL SKILLS", "CORE COMPETENCIES",
                "EDUCATION", "CERTIFICATIONS", "PROJECTS", "AWARDS", "PUBLICATIONS"
        };

        String upper = line.toUpperCase().replaceAll("[^A-Z\\s]", "").trim();

        for (String section : sections) {
            if (upper.equals(section) || upper.startsWith(section)) {
                return true;
            }
        }

        return false;
    }

    private String repeatChar(char c, int count) {
        return String.valueOf(c).repeat(Math.max(0, count));
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    /**
     * Calculates a simple keyword match score between resume and job description.
     */
    public double calculatedMatchScore(String resumeText, String jobDescription) {
        List<String> keywords = analyzeJobDescription(jobDescription);
        List<String> matched = mapKeywords(resumeText, keywords);

        if (keywords.isEmpty()) return 0.0;
        return (matched.size() / (double) keywords.size()) * 100.0;
    }
}