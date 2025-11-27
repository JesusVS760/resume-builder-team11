package dao;

import models.Resume;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResumeDAO {

    private final DatabaseConnection dbConnection;

    public ResumeDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public int saveResume(Resume resume) throws SQLException {
        String sql = """
            INSERT INTO resumes (user_id, file_name, file_path, uploaded_at)
            VALUES (?, ?, ?, COALESCE(?, datetime('now')))
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, resume.getUserId());
            ps.setString(2, resume.getFileName());
            ps.setString(3, resume.getFilePath());
            ps.setString(4, resume.getUploadedAt());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    resume.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public List<Resume> getResumesByUserOrderByDate(String userId) throws SQLException {
        String sql = """
            SELECT id, user_id, file_name, file_path, uploaded_at
            FROM resumes
            WHERE user_id = ?
            ORDER BY datetime(uploaded_at) DESC
            """;

        List<Resume> results = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<Resume> getResumesByUserOrderByName(String userId) throws SQLException {
        String sql = """
            SELECT id, user_id, file_name, file_path, uploaded_at
            FROM resumes
            WHERE user_id = ?
            ORDER BY LOWER(file_name) ASC, datetime(uploaded_at) DESC
            """;

        List<Resume> results = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public boolean deleteResume(int resumeId, String userId) throws SQLException {
        String sql = "DELETE FROM resumes WHERE id = ? AND user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resumeId);
            ps.setString(2, userId);

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    private Resume mapRow(ResultSet rs) throws SQLException {
        Resume r = new Resume();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getString("user_id"));
        r.setFileName(rs.getString("file_name"));
        r.setFilePath(rs.getString("file_path"));
        r.setUploadedAt(rs.getString("uploaded_at"));
        return r;
    }

    /**
     * Updates the file path for a resume (used when saving edited content to a new file)
     */
    public boolean updateResumeFilePath(int resumeId, String userId, String newFilePath) throws SQLException {
        String sql = "UPDATE resumes SET file_path = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newFilePath);
            ps.setInt(2, resumeId);
            ps.setString(3, userId);

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Gets a single resume by ID
     */
    public Resume getResumeById(int resumeId, String userId) throws SQLException {
        String sql = """
            SELECT id, user_id, file_name, file_path, uploaded_at
            FROM resumes
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resumeId);
            ps.setString(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
}
