package dao;
import models.Resume;
import java.sql.*;
import java.time.LocalDateTime;

public class ResumeDAO {
    private Connection connection;

    public ResumeDAO(Connection connection) {
        this.connection = connection;
    }

    public void save(Resume resume) throws SQLException {
        String sql = "INSERT INTO resumes (user_id, content, file_name, upload_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, resume.getUserId());
            stmt.setString(2, resume.getContent());
            stmt.setString(3, resume.getFileName());
            stmt.setTimestamp(4, Timestamp.valueOf(resume.getUploadDate()));
            stmt.executeUpdate();
        }
    }

    public Resume findById(int id) throws SQLException {
        String sql = "SELECT * FROM resumes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Resume resume = new Resume();
                resume.setId(rs.getInt("id"));
                resume.setUserId(rs.getInt("user_id"));
                resume.setContent(rs.getString("content"));
                resume.setFileName(rs.getString("file_name"));
                resume.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
                return resume;
            }
        }
        return null;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM resumes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
