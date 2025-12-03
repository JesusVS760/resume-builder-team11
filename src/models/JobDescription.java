// TODO: Implement JobDescription model class
// This class represents a job description in the system
// Fields: id, userId, title, company, description, keywords
package models;

import java.util.List;
import java.util.Objects;

/**
 * Represents a job description in the system.
 * Each JobDescription is associated with a user and can be used
 * to tailor resumes based on its keywords and description content.
 */
public class JobDescription {

    private int id;
    private int userId;
    private String title;
    private String company;
    private String description;
    private List<String> keywords;

    // Constructors
    public JobDescription() {}

    public JobDescription(int id, int userId, String title, String company, String description, List<String> keywords) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.company = company;
        this.description = description;
        this.keywords = keywords;
    }

    // Getters and Setters
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    // Utility Methods
    @Override
    public String toString() {
        return "JobDescription{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", description='" + description + '\'' +
                ", keywords=" + keywords +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobDescription)) return false;
        JobDescription that = (JobDescription) o;
        return id == that.id && userId == that.userId &&
                Objects.equals(title, that.title) &&
                Objects.equals(company, that.company) &&
                Objects.equals(description, that.description) &&
                Objects.equals(keywords, that.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, company, description, keywords);
    }
}
