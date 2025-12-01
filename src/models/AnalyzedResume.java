package models;

public class AnalyzedResume {
    private int id;
    private int userId;
    private int resumeId;          // FK to resumes.id
    private String jobTitle;
    private String jobCompany;
    private String jobDescription;
    private String tailoredText;   // the tailored resume content
    private String filePath;       // path to exported PDF/DOCX
    private String createdAt;      // TEXT from DB

    public AnalyzedResume() {}

    // Constructor for loading from DB
    public AnalyzedResume(int id, int userId, int resumeId, String jobTitle,
                          String jobCompany, String jobDescription,
                          String tailoredText, String filePath, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.resumeId = resumeId;
        this.jobTitle = jobTitle;
        this.jobCompany = jobCompany;
        this.jobDescription = jobDescription;
        this.tailoredText = tailoredText;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    // Constructor for inserting a new tailored resume
    public AnalyzedResume(int userId, int resumeId, String jobTitle,
                          String jobCompany, String jobDescription,
                          String tailoredText, String filePath) {
        this.userId = userId;
        this.resumeId = resumeId;
        this.jobTitle = jobTitle;
        this.jobCompany = jobCompany;
        this.jobDescription = jobDescription;
        this.tailoredText = tailoredText;
        this.filePath = filePath;
    }

    public AnalyzedResume(int resumeId, Object o, Object o1, String jobDesc, String tailoredText, Object o2) {
    }

    // Getters / setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobCompany() { return jobCompany; }
    public void setJobCompany(String jobCompany) { this.jobCompany = jobCompany; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getTailoredText() { return tailoredText; }
    public void setTailoredText(String tailoredText) { this.tailoredText = tailoredText; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
