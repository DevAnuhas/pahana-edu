package com.pahanaedu.dao;

import com.pahanaedu.model.Customer;
import com.pahanaedu.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Customer-related database operations
 */
public class CustomerDAO {
    private static final Logger LOGGER = Logger.getLogger(CustomerDAO.class.getName());

    public Customer findById(int id) {
        Customer customer = null;
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customer = mapResultSetToCustomer(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding customer by id: " + id, e);
        }

        return customer;
    }

    public Customer findByAccountNumber(String accountNumber) {
        Customer customer = null;
        String sql = "SELECT * FROM customers WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customer = mapResultSetToCustomer(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding customer by account number: " + accountNumber, e);
        }

        return customer;
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                customers.add(customer);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all customers", e);
        }

        return customers;
    }

    public List<Customer> searchCustomers(String searchTerm) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE account_number LIKE ? OR name LIKE ? OR telephone LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = mapResultSetToCustomer(rs);
                    customers.add(customer);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching customers with term: " + searchTerm, e);
        }

        return customers;
    }

    public boolean create(Customer customer) {
        String sql = "INSERT INTO customers (account_number, name, address, telephone, email, registration_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getAccountNumber());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getTelephone());
            stmt.setString(5, customer.getEmail());
            stmt.setDate(6, customer.getRegistrationDate());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating customer: " + customer.getName(), e);
        }

        return false;
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, address = ?, telephone = ?, email = ? " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getAddress());
            stmt.setString(3, customer.getTelephone());
            stmt.setString(4, customer.getEmail());
            stmt.setInt(5, customer.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating customer with ID: " + customer.getId(), e);
            return false;
        }
    }

    public boolean delete(int customerId) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting customer with ID: " + customerId, e);
            return false;
        }
    }

    public String generateAccountNumber() {
        // Format: CUS-YYYYMMDD-XXXX where XXXX is a sequential number
        String prefix = "CUS-";
        String datePart = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());

        String sql = "SELECT MAX(SUBSTRING_INDEX(account_number, '-', -1)) AS max_seq " +
                     "FROM customers WHERE account_number LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + datePart + "-%");

            try (ResultSet rs = stmt.executeQuery()) {
                int nextSeq = 1;
                if (rs.next()) {
                    String maxSeq = rs.getString("max_seq");
                    if (maxSeq != null) {
                        nextSeq = Integer.parseInt(maxSeq) + 1;
                    }
                }

                return String.format("%s%s-%04d", prefix, datePart, nextSeq);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating account number", e);
            // Fallback to timestamp-based account number if DB query fails
            return prefix + datePart + "-" + System.currentTimeMillis() % 10000;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setAccountNumber(rs.getString("account_number"));
        customer.setName(rs.getString("name"));
        customer.setAddress(rs.getString("address"));
        customer.setTelephone(rs.getString("telephone"));
        customer.setEmail(rs.getString("email"));
        customer.setRegistrationDate(rs.getDate("registration_date"));
        customer.setCreatedAt(rs.getTimestamp("created_at"));
        customer.setUpdatedAt(rs.getTimestamp("updated_at"));
        return customer;
    }
}
