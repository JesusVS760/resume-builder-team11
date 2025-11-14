package dao;

import models.Resume;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResumeDAO {

    // Insert a new resume, return generated id
    public int saveResume(Resume resume) throws SQLException {
        String sql = """
            INSERT INTO resumes (user_id, file_name, file_path)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, resume.getUserId());
            ps.setString(2, resume.getFileName());
            ps.setString(3, resume.getFilePath());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Inserting resume failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    resume.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Inserting resume failed, no ID obtained.");
    }

    // Get one resume by id
    public Resume getResumeById(int id) throws SQLException {
        String sql = """
            SELECT id, user_id, file_name, file_path, uploaded_at
            FROM resumes
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Get all resumes for a user
    public List<Resume> getResumesByUser(int userId) throws SQLException {
        String sql = """
            SELECT id, user_id, file_name, file_path, uploaded_at
            FROM resumes
            WHERE user_id = ?
            ORDER BY uploaded_at DESC
        """;

        List<Resume> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }

        return result;
    }

    // Delete a resume (and enforce ownership)
    public boolean deleteResume(int resumeId, int userId) throws SQLException {
        String sql = "DELETE FROM resumes WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resumeId);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    private Resume mapRow(ResultSet rs) throws SQLException {
        Resume r = new Resume();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setFileName(rs.getString("file_name"));
        r.setFilePath(rs.getString("file_path"));
        r.setUploadedAt(rs.getString("uploaded_at"));
        return r;
    }

    public List<Resume> getResumesByUserOrderByDate(int userId) throws SQLException {
        String sql = """
        SELECT id, user_id, file_name, file_path, uploaded_at
        FROM resumes
        WHERE user_id = ?
        ORDER BY datetime(uploaded_at) DESC
        """;

        List<Resume> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<Resume> getResumesByUserOrderByName(int userId) throws SQLException {
        String sql = """
        SELECT id, user_id, file_name, file_path, uploaded_at
        FROM resumes
        WHERE user_id = ?
        ORDER BY LOWER(file_name) ASC, datetime(uploaded_at) DESC
        """;

        List<Resume> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

}
