package com.pahanaedu.dao;

import com.pahanaedu.model.User;
import com.pahanaedu.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User-related database operations
 */
public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public User findByUsername(String username) {
        User user = null;
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
        }

        return user;
    }

    public User findById(int id) {
        User user = null;
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by id: " + id, e);
        }

        return user;
    }

    public User authenticate(String username, String password) {
        User user = findByUsername(username);

        if (user != null) {
            boolean isPasswordValid = verifyPassword(password, user.getPassword());
            
            if (isPasswordValid) {
                user.setPassword(null);
                return user;
            }
        }

        return null;
    }

    private boolean verifyPassword(String userPassword, String dbPassword) {
        if (dbPassword == null || userPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(userPassword, dbPassword);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error verifying password", e);
            return false;
        }
    }
    
    /**
     * Hash a password using BCrypt with salt rounds 10
     * This method can be used for user registration or password updates
     */
    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));

        return user;
    }
}
