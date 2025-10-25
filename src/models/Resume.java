package models;

import java.time.LocalDateTime;

public class Resume {
    private int id;                 // Unique identifier for the resume
    private int userId;             // User who uploaded the resume
    private String content;         // Parsed or raw text content
    private String fileName;        // Original uploaded file name
    private LocalDateTime uploadDate; // Timestamp of upload

    // constructors
    public Resume() {}

    public Resume(int id, int userId, String content, String fileName, LocalDateTime uploadDate) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.fileName = fileName;
        this.uploadDate = uploadDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public String toString() {
        return "Resume{" +
                "id=" + id +
                ", userId=" + userId +
                ", fileName='" + fileName + '\'' +
                ", uploadDate=" + uploadDate +
                '}';
    }
}
