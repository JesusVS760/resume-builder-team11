/*
 * DISCLAIMER:
 * Portions of this project were supported by the use of Claude Sonnet 4.5.
 * The tool was used strictly for assistance with formatting, answering
 * technical questions, and occasional guidance during implementation.
 * HEAVY USAGE IN FORMATTING AS IT WAS VERY TEDIOUS AND DIFFICULT TO MANAGE FORMATTING
 * ALL CONTENT WAS THOROUGHLY READ AND LEARNED THROUGH THE PROCESS
 */

package services;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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
     * Generates a complete, professionally formatted tailored resume with feedback.
     */
    public String tailorResume(ResumeParserService.ParsedResume parsedResume, String jobDescription) {
        if (parsedResume == null || parsedResume.getFullText() == null) {
            return "Error: Resume is empty or invalid.";
        }

        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return formatProfessionalResume(parsedResume, Collections.emptyList(), jobDescription);
        }

        String originalResume = parsedResume.getFullText();
        List<String> jobKeywords = analyzeJobDescription(jobDescription);
        List<String> matchedKeywords = mapKeywords(originalResume, jobKeywords);

        // Build output with clear separation markers
        StringBuilder output = new StringBuilder();

        // Add feedback section with clear markers
        output.append("===FEEDBACK_START===\n");
        output.append(buildFeedbackSection(parsedResume, matchedKeywords, jobKeywords, jobDescription));
        output.append("===FEEDBACK_END===\n\n");

        // Add the tailored resume with clear markers
        output.append("===RESUME_START===\n");
        output.append(formatProfessionalResume(parsedResume, matchedKeywords, jobDescription));
        output.append("===RESUME_END===\n");

        return output.toString();
    }

    /**
     * Builds the feedback section showing match analysis and keywords
     */
    private String buildFeedbackSection(ResumeParserService.ParsedResume parsedResume,
                                        List<String> matchedKeywords,
                                        List<String> allJobKeywords,
                                        String jobDescription) {
        StringBuilder feedback = new StringBuilder();

        double matchScore = calculatedMatchScore(parsedResume.getFullText(), jobDescription);

        feedback.append("Job Match Score: ").append(String.format("%.1f%%", matchScore)).append("\n");
        feedback.append("Keywords Matched: ").append(matchedKeywords.size())
                .append("/").append(allJobKeywords.size()).append("\n\n");

        // Show matched keywords
        if (!matchedKeywords.isEmpty()) {
            feedback.append("MATCHED KEYWORDS (Already in your resume):\n");
            for (String keyword : matchedKeywords) {
                feedback.append("  • ").append(capitalize(keyword)).append("\n");
            }
            feedback.append("\n");
        }

        // Show missing keywords
        List<String> missingKeywords = allJobKeywords.stream()
                .filter(kw -> !matchedKeywords.contains(kw))
                .collect(Collectors.toList());

        if (!missingKeywords.isEmpty()) {
            feedback.append("KEYWORDS TO CONSIDER ADDING:\n");
            for (String keyword : missingKeywords) {
                feedback.append("  • ").append(capitalize(keyword)).append("\n");
            }
        }

        return feedback.toString();
    }

    /**
     * Formats the resume in a professional layout with proper bullet points and sections
     */
    private String formatProfessionalResume(ResumeParserService.ParsedResume parsedResume,
                                            List<String> matchedKeywords,
                                            String jobDescription) {
        StringBuilder resume = new StringBuilder();
        Map<String, String> sections = parsedResume.getSections();

        // HEADER
        String header = extractHeader(parsedResume.getFullText());
        if (header != null && !header.trim().isEmpty()) {
            resume.append(header);
            resume.append("\n").append(repeatChar('═', 80)).append("\n\n");
        }

        // PROFESSIONAL SUMMARY
        String summary = sections.get("summary");
        if (summary != null && !summary.trim().isEmpty()) {
            resume.append("PROFESSIONAL SUMMARY\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatSummary(summary, matchedKeywords)).append("\n\n");
        } else if (!matchedKeywords.isEmpty()) {
            resume.append("PROFESSIONAL SUMMARY\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append("Results-driven professional with expertise in ");
            resume.append(matchedKeywords.stream()
                    .limit(8)
                    .map(this::capitalize)
                    .collect(Collectors.joining(", ")));
            resume.append(". Proven track record of delivering high-quality solutions and driving business success.\n\n");
        }

        // CORE COMPETENCIES / SKILLS
        String skills = sections.get("skills");
        if (skills != null && !skills.trim().isEmpty()) {
            resume.append("CORE COMPETENCIES\n");
            resume.append(repeatChar('─', 80)).append("\n");
            String formattedSkills = formatSkillsSection(skills, matchedKeywords);
            if (!formattedSkills.trim().isEmpty()) {
                resume.append(formattedSkills).append("\n");
            }
        }

        // PROFESSIONAL EXPERIENCE
        // Check multiple possible keys for experience section
        String experience = sections.get("experience");
        if (experience == null || experience.trim().isEmpty()) {
            experience = sections.get("work experience");
        }
        if (experience == null || experience.trim().isEmpty()) {
            experience = sections.get("professional experience");
        }

        boolean hasExperience = false;

        if (experience != null && !experience.trim().isEmpty()) {
            resume.append("PROFESSIONAL EXPERIENCE\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatExperienceSection(experience, matchedKeywords));
            hasExperience = true;
        } else if (!parsedResume.getExperiences().isEmpty()) {
            resume.append("PROFESSIONAL EXPERIENCE\n");
            resume.append(repeatChar('─', 80)).append("\n");
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < parsedResume.getExperiences().size(); i++) {
                String exp = parsedResume.getExperiences().get(i);
                if (!seen.contains(exp.trim())) {
                    resume.append(formatExperienceEntry(exp, matchedKeywords));
                    if (i < parsedResume.getExperiences().size() - 1) {
                        resume.append("\n");
                    }
                    seen.add(exp.trim());
                }
            }
            hasExperience = true;
        }

        // If still no experience, try projects section as fallback
        if (!hasExperience) {
            String projects = sections.get("projects");
            if (projects != null && !projects.trim().isEmpty()) {
                resume.append("PROFESSIONAL EXPERIENCE\n");
                resume.append(repeatChar('─', 80)).append("\n");
                resume.append(formatExperienceSection(projects, matchedKeywords));
                hasExperience = true;
            }
        }

        // EDUCATION
        String education = sections.get("education");
        if (education != null && !education.trim().isEmpty()) {
            resume.append("\nEDUCATION\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatEducationSection(education)).append("\n");
        }

        // CERTIFICATIONS
        String certifications = sections.get("certifications");
        if (certifications != null && !certifications.trim().isEmpty()) {
            resume.append("CERTIFICATIONS\n");
            resume.append(repeatChar('─', 80)).append("\n");
            resume.append(formatBulletSection(certifications)).append("\n");
        }

        // PROJECTS (only if not already included in experience)
        if (!hasExperience) {
            String projects = sections.get("projects");
            if (projects != null && !projects.trim().isEmpty()) {
                resume.append("KEY PROJECTS\n");
                resume.append(repeatChar('─', 80)).append("\n");
                resume.append(formatProjectsSection(projects, matchedKeywords));
            }
        }

        // FOOTER with Match Score
        resume.append("\n").append(repeatChar('═', 80)).append("\n");
        if (!matchedKeywords.isEmpty() && jobDescription != null && !jobDescription.trim().isEmpty()) {
            double matchScore = calculatedMatchScore(parsedResume.getFullText(), jobDescription);
            List<String> allJobKeywords = analyzeJobDescription(jobDescription);
            resume.append(String.format("Job Match Score: %.1f%% | Keywords Matched: %d/%d\n",
                    matchScore, matchedKeywords.size(), allJobKeywords.size()));
        }

        return resume.toString();
    }

    // Helper Methods

    private String extractHeader(String resumeText) {
        String[] lines = resumeText.split("\n");
        StringBuilder header = new StringBuilder();

        int headerLines = 0;
        for (int i = 0; i < Math.min(10, lines.length) && headerLines < 6; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            if (isSectionHeader(line)) break;
            // Left-align all header lines (no centering)
            if (headerLines == 0 && line.length() > 3) {
                header.append(line.toUpperCase()).append("\n");
            } else {
                header.append(line).append("\n");
            }
            headerLines++;
        }

        return header.toString();
    }

    private String formatSummary(String summary, List<String> matchedKeywords) {
        String cleaned = summary.trim().replaceAll("\\s+", " ");
        return wrapText(cleaned, 80);
    }

    private String formatSkillsSection(String skills, List<String> matchedKeywords) {
        List<String> skillList = extractSkills(skills);

        // Remove duplicates and empty entries
        skillList = skillList.stream()
                .distinct()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        StringBuilder formatted = new StringBuilder();

        // Format in 2 columns for better readability - all with regular bullets
        for (int i = 0; i < skillList.size(); i += 2) {
            String skill1 = skillList.get(i);
            formatted.append("• ").append(skill1);

            // Add proper spacing to reach column 2
            int padding = 38 - ("• " + skill1).length();
            if (padding > 0) {
                formatted.append(" ".repeat(padding));
            }

            if (i + 1 < skillList.size()) {
                String skill2 = skillList.get(i + 1);
                formatted.append("• ").append(skill2);
            }
            formatted.append("\n");
        }

        return formatted.toString();
    }

    /**
     * Better skill extraction - handles categories, semicolons, commas, bullets and merges fragments
     * DISCLAIMER CODE GENERATED WITH CALUDE SONNET 4.5
     */
    private List<String> extractSkills(String skillsText) {
        // First normalize: replace newlines and multiple spaces with single space
        String normalized = skillsText.replaceAll("\\s+", " ").trim();

        // Remove existing bullets
        normalized = normalized.replaceAll("[•★●■▪]", " ");

        // Clean up multiple spaces and ampersands at the start
        normalized = normalized.replaceAll("\\s+", " ").replaceAll("^[&\\s]+", "").trim();

        // Fix common fragmented skills
        normalized = normalized.replaceAll("Problem\\s+solving", "Problem-solving");

        // Handle category-based format (e.g., "Languages: JavaScript, Python")
        // Remove category labels but keep the skills
        normalized = normalized.replaceAll("(Languages|Frameworks|Tools|APIs?\\s*&?\\s*Services|Skills|Technologies)\\s*:\\s*", "");

        // Split by semicolons, commas, and pipes
        String[] parts = normalized.split("[;,|]+");

        List<String> skills = new ArrayList<>();

        for (String part : parts) {
            String skill = part.trim();

            // Remove any leading dashes or special chars
            skill = skill.replaceAll("^[-–—&\\s]+", "").trim();

            // Skip invalid entries
            if (skill.isEmpty() || skill.length() < 2 || skill.length() > 60 ||
                    skill.equals("&") || skill.matches("^(solving|Communication|communication)$")) {
                continue;
            }

            // Skip if it's just a category label that wasn't caught
            if (skill.matches("(?i)(languages|frameworks|tools|apis?\\s*&?\\s*services|skills|technologies)")) {
                continue;
            }

            skills.add(skill);
        }

        return skills;
    }

    /**
     * Properly identifies and separates distinct project/job experiences
     */
    private String formatExperienceSection(String experience, List<String> matchedKeywords) {
        StringBuilder formatted = new StringBuilder();
        List<String> projects = splitIntoProjects(experience);

        for (int i = 0; i < projects.size(); i++) {
            String project = projects.get(i).trim();
            if (!project.isEmpty()) {
                formatted.append(formatSingleProject(project, matchedKeywords));

                // Add blank line between projects (but not after last one)
                if (i < projects.size() - 1) {
                    formatted.append("\n");
                }
            }
        }

        return formatted.toString();
    }

    /**
     * Splits experience text into distinct projects by identifying project titles
     */
    private List<String> splitIntoProjects(String experience) {
        List<String> projects = new ArrayList<>();

        // Pattern to match project titles (usually start with capital letter, end with dash or colon)
        Pattern projectPattern = Pattern.compile("^([A-Z][^\\n-]+ -|[A-Z][^\\n:]+:)", Pattern.MULTILINE);
        Matcher matcher = projectPattern.matcher(experience);

        List<Integer> starts = new ArrayList<>();
        while (matcher.find()) {
            starts.add(matcher.start());
        }

        if (starts.isEmpty()) {
            // No clear project boundaries, treat as one
            projects.add(experience);
        } else {
            // Split at project boundaries
            for (int i = 0; i < starts.size(); i++) {
                int start = starts.get(i);
                int end = (i + 1 < starts.size()) ? starts.get(i + 1) : experience.length();
                String project = experience.substring(start, end).trim();
                if (!project.isEmpty()) {
                    projects.add(project);
                }
            }
        }

        return projects;
    }

    /**
     * UPDATED: Formats a single project with clean bullets (•) only - no stars
     */
    private String formatSingleProject(String projectText, List<String> matchedKeywords) {
        StringBuilder formatted = new StringBuilder();

        String[] lines = projectText.split("\n");
        String projectTitle = null;
        int contentStartIndex = 0;

        // Extract project title (usually first line with " - " or ending with ":")
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.contains(" - ") || firstLine.endsWith(":")) {
                // Extract just the title part (before " - " or ":")
                if (firstLine.contains(" - ")) {
                    projectTitle = firstLine.substring(0, firstLine.indexOf(" - ")).trim();
                } else {
                    projectTitle = firstLine.replace(":", "").trim();
                }
                contentStartIndex = 1;
            }
        }

        // Add project title on its own line
        if (projectTitle != null && !projectTitle.isEmpty()) {
            formatted.append(projectTitle).append("\n");
        }

        // Gather all content after title
        StringBuilder allContent = new StringBuilder();
        for (int i = contentStartIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                // Remove existing bullets
                line = line.replaceAll("^[•★●■▪-]\\s*", "");

                if (allContent.length() > 0 && !allContent.toString().endsWith(" ")) {
                    allContent.append(" ");
                }
                allContent.append(line);
            }
        }

        // Split content into sentences for bullets
        String content = allContent.toString();
        List<String> bullets = new ArrayList<>();

        // Split by periods followed by space and capital letter (sentence boundaries)
        String[] sentences = content.split("(?<=\\.)\\s+(?=[A-Z])");

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                // Ensure ends with period
                if (!sentence.endsWith(".")) {
                    sentence += ".";
                }
                bullets.add(sentence);
            }
        }

        // Format all bullets with regular bullets (•) - no stars
        for (String bullet : bullets) {
            if (bullet.isEmpty()) continue;
            formatted.append(wrapBullet("• " + bullet, 80, 2)).append("\n");
        }

        return formatted.toString();
    }

    private String formatExperienceEntry(String text, List<String> matchedKeywords) {
        return formatSingleProject(text, matchedKeywords);
    }

    private String formatProjectsSection(String projects, List<String> matchedKeywords) {
        return formatExperienceSection(projects, matchedKeywords);
    }

    private String formatEducationSection(String education) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = education.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                formatted.append(trimmed).append("\n");
            }
        }

        return formatted.toString();
    }

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

    private String wrapBullet(String text, int width, int indent) {
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split("\\s+");
        int lineLength = 0;
        String indentStr = " ".repeat(indent);
        boolean firstLine = true;

        for (String word : words) {
            if (lineLength + word.length() + 1 > width) {
                wrapped.append("\n");
                if (!firstLine) {
                    wrapped.append(indentStr);
                    lineLength = indent;
                } else {
                    lineLength = 0;
                }
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

    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private boolean isSectionHeader(String line) {
        if (line.length() < 3 || line.length() > 50) return false;
        String[] sections = {
                "SUMMARY", "OBJECTIVE", "PROFILE", "EXPERIENCE", "WORK EXPERIENCE",
                "EMPLOYMENT", "SKILLS", "TECHNICAL SKILLS", "CORE COMPETENCIES",
                "EDUCATION", "CERTIFICATIONS", "PROJECTS", "AWARDS", "PUBLICATIONS"
        };

        String upper = line.toUpperCase().replaceAll("[^A-Z\\s]", "").trim();
        for (String section : sections) {
            if (upper.equals(section) || upper.startsWith(section)) return true;
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

    public double calculatedMatchScore(String resumeText, String jobDescription) {
        List<String> keywords = analyzeJobDescription(jobDescription);
        List<String> matched = mapKeywords(resumeText, keywords);
        if (keywords.isEmpty()) return 0.0;
        return (matched.size() / (double) keywords.size()) * 100.0;
    }
}