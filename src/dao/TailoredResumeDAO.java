package dao;

import models.TailoredResume;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TailoredResumeDAO {

    public int saveTailoredResume(TailoredResume tr) throws SQLException {
        String sql = """
        INSERT INTO tailored_resumes
            (user_id, resume_id, job_title, job_company,
             job_description, tailored_text, file_path)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tr.getUserId());
            ps.setInt(2, tr.getResumeId());
            ps.setString(3, tr.getJobTitle());
            ps.setString(4, tr.getJobCompany());
            ps.setString(5, tr.getJobDescription());

            // 🔹 IMPORTANT: avoid NULL for NOT NULL column
            String safeTailored = tr.getTailoredText();
            if (safeTailored == null) {
                System.out.println("DEBUG: tailoredText is null in saveTailoredResume for resumeId=" + tr.getResumeId());
                safeTailored = ""; // or some placeholder
            }
            ps.setString(6, safeTailored);

            ps.setString(7, tr.getFilePath());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Inserting tailored resume failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    tr.setId(id);
                    return id;
                } else {
                    throw new SQLException("Inserting tailored resume failed, no ID obtained.");
                }
            }
        }
    }

    public List<TailoredResume> getTailoredByUser(int userId) throws SQLException {
        String sql = """
            SELECT id, user_id, resume_id, job_title, job_company,
                   job_description, tailored_text, file_path, created_at
            FROM tailored_resumes
            WHERE user_id = ?
            ORDER BY created_at DESC
        """;

        List<TailoredResume> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public List<TailoredResume> getTailoredByUserAndResume(int userId, int resumeId) throws SQLException {
        String sql = """
            SELECT id, user_id, resume_id, job_title, job_company,
                   job_description, tailored_text, file_path, created_at
            FROM tailored_resumes
            WHERE user_id = ? AND resume_id = ?
            ORDER BY created_at DESC
        """;

        List<TailoredResume> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, resumeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public boolean deleteTailoredResume(int id, int userId) throws SQLException {
        String sql = "DELETE FROM tailored_resumes WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    private TailoredResume mapRow(ResultSet rs) throws SQLException {
        TailoredResume tr = new TailoredResume();
        tr.setId(rs.getInt("id"));
        tr.setUserId(rs.getInt("user_id"));
        tr.setResumeId(rs.getInt("resume_id"));
        tr.setJobTitle(rs.getString("job_title"));
        tr.setJobCompany(rs.getString("job_company"));
        tr.setJobDescription(rs.getString("job_description"));
        tr.setTailoredText(rs.getString("tailored_text"));
        tr.setFilePath(rs.getString("file_path"));
        tr.setCreatedAt(rs.getString("created_at"));
        return tr;
    }
}
