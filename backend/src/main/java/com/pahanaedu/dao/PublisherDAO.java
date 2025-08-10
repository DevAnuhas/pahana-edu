package com.pahanaedu.dao;

import com.pahanaedu.model.Publisher;
import com.pahanaedu.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Publisher-related database operations
 */
public class PublisherDAO {
    private static final Logger LOGGER = Logger.getLogger(PublisherDAO.class.getName());

    public Publisher findById(int id) {
        Publisher publisher = null;
        String sql = "SELECT * FROM publishers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    publisher = mapResultSetToPublisher(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding publisher by id: " + id, e);
        }

        return publisher;
    }

    public List<Publisher> findAll() {
        List<Publisher> publishers = new ArrayList<>();
        String sql = "SELECT * FROM publishers ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Publisher publisher = mapResultSetToPublisher(rs);
                publishers.add(publisher);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all publishers", e);
        }

        return publishers;
    }

    public boolean create(Publisher publisher) {
        String sql = "INSERT INTO publishers (name, contact_person, telephone, email, address) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, publisher.getName());
            stmt.setString(2, publisher.getContactPerson());
            stmt.setString(3, publisher.getTelephone());
            stmt.setString(4, publisher.getEmail());
            stmt.setString(5, publisher.getAddress());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        publisher.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating publisher: " + publisher.getName(), e);
        }

        return false;
    }

    public boolean update(Publisher publisher) {
        String sql = "UPDATE publishers SET name = ?, contact_person = ?, telephone = ?, " +
                     "email = ?, address = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publisher.getName());
            stmt.setString(2, publisher.getContactPerson());
            stmt.setString(3, publisher.getTelephone());
            stmt.setString(4, publisher.getEmail());
            stmt.setString(5, publisher.getAddress());
            stmt.setInt(6, publisher.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating publisher with ID: " + publisher.getId(), e);
            return false;
        }
    }

    public boolean delete(int publisherId) {
        // First check if there are any books using this publisher
        String checkSql = "SELECT COUNT(*) FROM books WHERE publisher_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, publisherId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Publisher is in use, cannot delete
                    LOGGER.log(Level.WARNING, "Cannot delete publisher ID " + publisherId + " as it is used by books");
                    return false;
                }
            }

            // If we get here, the publisher is not in use and can be deleted
            String sql = "DELETE FROM publishers WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, publisherId);
                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting publisher with ID: " + publisherId, e);
            return false;
        }
    }

    private Publisher mapResultSetToPublisher(ResultSet rs) throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setId(rs.getInt("id"));
        publisher.setName(rs.getString("name"));
        publisher.setContactPerson(rs.getString("contact_person"));
        publisher.setTelephone(rs.getString("telephone"));
        publisher.setEmail(rs.getString("email"));
        publisher.setAddress(rs.getString("address"));
        publisher.setCreatedAt(rs.getTimestamp("created_at"));
        publisher.setUpdatedAt(rs.getTimestamp("updated_at"));
        return publisher;
    }
}
