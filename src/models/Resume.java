package models;

public class Resume {
    private int id;
    private String userId;
    private String fileName;
    private String filePath;
    private String uploadedAt;

    public Resume() {}

    public Resume(String userId, String fileName, String filePath) {
        this.userId = userId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    // getters & setters...

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
}
