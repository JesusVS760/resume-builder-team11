package dao;

import models.User;
import java.sql.*;

public class UserDAO {
    private DatabaseConnection dbConnection;

    public UserDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public boolean saveUser(User user) {
        // Generate the next available user ID
        String userId = getNextUserId();
        user.setId(userId); // Set the generated ID on the user object

        String sql = "INSERT INTO users (id, email, password_hash, name, email_verified, text_verified) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getName());

            // Convert booleans to integers (0 or 1)
            ps.setInt(5, user.isEmailVerified() ? 1 : 0);
            ps.setInt(6, user.isTextVerified() ? 1 : 0);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private String getNextUserId() {
        String sql = "SELECT id FROM users WHERE id LIKE 'U%' ORDER BY CAST(SUBSTR(id, 2) AS INTEGER) DESC LIMIT 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString(1);
                // Extract the number from "U123" -> 123, then add 1
                int lastNumber = Integer.parseInt(lastId.substring(1));
                int nextNumber = lastNumber + 1;
                return "U" + nextNumber;
            }

        } catch (SQLException e) {
            System.err.println("Error getting next user ID: " + e.getMessage());
        }
        return "U1"; // Default to U1 if no users exist or error
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExists(String email) {
        // Check both users and oauth_users tables
        String sqlUsers = "SELECT COUNT(*) FROM users WHERE email = ?";
        String sqlOAuthUsers = "SELECT COUNT(*) FROM oauth_users WHERE oauth_email = ?";

        try (Connection conn = dbConnection.getConnection()) {

            // Check regular users table
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsers)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }

            // Check OAuth users table
            try (PreparedStatement stmt = conn.prepareStatement(sqlOAuthUsers)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking if email exists: " + e.getMessage());
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
        return false;
    }

    public boolean updateEmailVerification(String email, boolean verified) {
        String sql = "UPDATE users SET email_verified = ? WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, verified);
            stmt.setString(2, email);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating email verification: " + e.getMessage());
        }
        return false;
    }

    public User findByOAuthEmail(String provider, String email) {
        String sql = "SELECT * FROM oauth_users WHERE oauth_provider = ? AND oauth_email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, provider);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create a User object from oauth_users table
                User user = new User();
                user.setId(rs.getString("id"));
                user.setEmail(rs.getString("oauth_email"));
                user.setName(rs.getString("name"));
                user.setOauthProvider(rs.getString("oauth_provider"));
                user.setOauthEmail(rs.getString("oauth_email"));
                user.setEmailVerified(true); // OAuth users are always verified
                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error finding OAuth user: " + e.getMessage());
        }
        return null;
    }

    public User findById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean saveOAuthUser(String provider, String oauthEmail, String name) {
        // Generate prefixed ID (O1, O2, O3...)
        String oauthUserId = "O" + getNextOAuthUserId();

        String sql = "INSERT INTO oauth_users (id, oauth_provider, oauth_email, name) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, oauthUserId);
            stmt.setString(2, provider);
            stmt.setString(3, oauthEmail);
            stmt.setString(4, name);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Return the generated ID somehow, or just return success
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error saving OAuth user: " + e.getMessage());
        }
        return false;
    }

    private int getNextOAuthUserId() {
        String sql = "SELECT COUNT(*) FROM oauth_users WHERE id LIKE 'O%'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1; // Next available number
            }

        } catch (SQLException e) {
            System.err.println("Error getting next OAuth user ID: " + e.getMessage());
        }
        return 1; // Default to 1 if error
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setName(rs.getString("name"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setPhoneVerified(rs.getBoolean("text_verified"));
        return user;
    }
}
