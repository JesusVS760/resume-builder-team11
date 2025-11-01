// TODO: Implement TailoredResume model class
// This class represents a tailored resume result
// Fields: id, resumeId, jobDescId, tailoredContent, createdDate

package models;

import com.twilio.rest.api.v2010.account.incomingphonenumber.Local;

import java.time.LocalDateTime;
import java.util.Objects;

public class TailoredResume {

    private Long id;
    private Long resumeId;
    private Long jobDescId;
    private String tailoredContent;
    private LocalDateTime createdDate;

    // constructors

    public TailoredResume() {
        this.createdDate = LocalDateTime.now();
    }

    // all fields except id (new records)
    public TailoredResume(Long resumeId, Long jobDescId, String tailoredContent) {
        this.resumeId = resumeId;
        this.jobDescId = jobDescId;
        this.tailoredContent = tailoredContent;
        this.createdDate = LocalDateTime.now();
    }

    // full constructor

    public TailoredResume(Long id, Long resumeId, Long jobDescId, String tailoredContent, LocalDateTime createDate) {
        this.id = id;
        this.resumeId = resumeId;
        this.jobDescId = jobDescId;
        this.tailoredContent = tailoredContent;
        this.createdDate = createDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public Long getJobDescId() {
        return jobDescId;
    }

    public void setJobDescId(Long jobDescId) {
        this.jobDescId = jobDescId;
    }

    public String getTailoredContent() {
        return tailoredContent;
    }

    public void setTailoredContent(String tailoredContent) {
        this.tailoredContent = tailoredContent;
    }

    public LocalDateTime getCreateDate() {
        return createdDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createdDate = createDate;
    }

    // validates that all required fields are present

    public boolean isValid() {
        return resumeId != null
        && jobDescId != null
        && tailoredContent != null
        && !tailoredContent.trim().isEmpty()
        && createdDate != null;
    }

     @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TailoredResume that = (TailoredResume) o;
        return Objects.equals(id, that.id)
                && Objects.equals(resumeId, that.resumeId)
                && Objects.equals(jobDescId, that.jobDescId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resumeId, jobDescId);
    }

    @Override
    public String toString() {
        return "TailoredResume{" +
                "id=" + id +
                ", resumeId=" + resumeId +
                ", jobDescId=" + jobDescId +
                ", tailoredContentLength=" + (tailoredContent != null ? tailoredContent.length() : 0) +
                ", createdDate=" + createdDate +
                '}';
    }
}