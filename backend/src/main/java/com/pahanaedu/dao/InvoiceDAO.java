package com.pahanaedu.dao;

import com.pahanaedu.model.Invoice;
import com.pahanaedu.model.InvoiceItem;
import com.pahanaedu.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Invoice-related database operations
 */
public class InvoiceDAO {
    private static final Logger LOGGER = Logger.getLogger(InvoiceDAO.class.getName());
    private final InvoiceItemDAO invoiceItemDAO = new InvoiceItemDAO();

    public Invoice findById(int id) {
        Invoice invoice = null;
        String sql = "SELECT i.*, c.name AS customer_name, u.full_name AS cashier_name " +
                    "FROM invoices i " +
                    "LEFT JOIN customers c ON i.customer_id = c.id " +
                    "JOIN users u ON i.cashier_id = u.id " +
                    "WHERE i.id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    invoice = mapResultSetToInvoice(rs);
                    // Load invoice items
                    List<InvoiceItem> items = invoiceItemDAO.findByInvoiceId(invoice.getId());
                    invoice.setItems(items);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by id: " + id, e);
        }

        return invoice;
    }

    public Invoice findByInvoiceNumber(String invoiceNumber) {
        Invoice invoice = null;
        String sql = "SELECT i.*, c.name AS customer_name, u.full_name AS cashier_name " +
                    "FROM invoices i " +
                    "LEFT JOIN customers c ON i.customer_id = c.id " +
                    "JOIN users u ON i.cashier_id = u.id " +
                    "WHERE i.invoice_number = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, invoiceNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    invoice = mapResultSetToInvoice(rs);
                    // Load invoice items
                    List<InvoiceItem> items = invoiceItemDAO.findByInvoiceId(invoice.getId());
                    invoice.setItems(items);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by number: " + invoiceNumber, e);
        }

        return invoice;
    }

    public List<Invoice> findAll() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, c.name AS customer_name, u.full_name AS cashier_name " +
                    "FROM invoices i " +
                    "LEFT JOIN customers c ON i.customer_id = c.id " +
                    "JOIN users u ON i.cashier_id = u.id " +
                    "ORDER BY i.invoice_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoices.add(invoice);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all invoices", e);
        }

        return invoices;
    }

    public List<Invoice> findByCustomerId(int customerId) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, c.name AS customer_name, u.full_name AS cashier_name " +
                    "FROM invoices i " +
                    "LEFT JOIN customers c ON i.customer_id = c.id " +
                    "JOIN users u ON i.cashier_id = u.id " +
                    "WHERE i.customer_id = ? " +
                    "ORDER BY i.invoice_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    invoices.add(invoice);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoices by customer ID: " + customerId, e);
        }

        return invoices;
    }

    public boolean create(Invoice invoice, Connection conn) throws SQLException {
        String sql = "INSERT INTO invoices (invoice_number, customer_id, cashier_id, invoice_date, " +
                    "subtotal, discount_amount, tax_amount, total_amount, payment_method, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, invoice.getInvoiceNumber());

            if (invoice.getCustomerId() != null) {
                stmt.setInt(2, invoice.getCustomerId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            stmt.setInt(3, invoice.getCashierId());
            stmt.setTimestamp(4, invoice.getInvoiceDate());
            stmt.setBigDecimal(5, invoice.getSubtotal());
            stmt.setBigDecimal(6, invoice.getDiscountAmount());
            stmt.setBigDecimal(7, invoice.getTaxAmount());
            stmt.setBigDecimal(8, invoice.getTotalAmount());
            stmt.setString(9, invoice.getPaymentMethod());
            stmt.setString(10, invoice.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        invoice.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean create(Invoice invoice) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            LOGGER.info("Starting invoice creation transaction");

            // Generate invoice number if not provided
            if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
                invoice.setInvoiceNumber(generateInvoiceNumber());
            }

            // Create invoice record
            boolean invoiceCreated = create(invoice, conn);
            LOGGER.info("Invoice record created: " + invoiceCreated);

            if (invoiceCreated && invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                // Create invoice items and update stock within the same transaction
                for (InvoiceItem item : invoice.getItems()) {
                    item.setInvoiceId(invoice.getId());
                    boolean itemCreated = invoiceItemDAO.create(item, conn);

                    if (!itemCreated) {
                        throw new SQLException("Failed to create invoice item for book ID: " + item.getBookId());
                    }

                    // Update book stock using the same connection to maintain transaction
                    updateBookStockInTransaction(conn, item.getBookId(), -item.getQuantity());
                }
            }

            conn.commit();
            success = true;
            LOGGER.info("Invoice transaction committed successfully");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating invoice: " + e.getMessage(), e);

            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.info("Transaction rolled back");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
                // Don't close the connection here if it's managed by connection pool
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }

        return success;
    }

    /**
     * Update book stock within the same transaction
     */
    private void updateBookStockInTransaction(Connection conn, int bookId, int quantityChange) throws SQLException {
        String sql = "UPDATE books SET stock_quantity = stock_quantity + ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, bookId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to update stock for book ID: " + bookId);
            }

            LOGGER.info("Updated stock for book ID " + bookId + " by " + quantityChange);
        }
    }

    public boolean delete(int invoiceId) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Get invoice to restore stock quantities
            Invoice invoice = findById(invoiceId);
            if (invoice != null && invoice.getItems() != null) {
                BookDAO bookDAO = new BookDAO();

                // Restore stock quantities
                for (InvoiceItem item : invoice.getItems()) {
                    bookDAO.updateStock(item.getBookId(), item.getQuantity()); // Add back to stock
                }

                // Delete invoice (cascade will delete items)
                String sql = "DELETE FROM invoices WHERE id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, invoiceId);
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        conn.commit();
                        success = true;
                    } else {
                        conn.rollback();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting invoice with ID: " + invoiceId, e);

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }

        return success;
    }

    public String generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-XXXX where XXXX is a sequential number
        String prefix = "INV-";
        String datePart = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());

        String sql = "SELECT MAX(SUBSTRING_INDEX(invoice_number, '-', -1)) AS max_seq " +
                     "FROM invoices WHERE invoice_number LIKE ?";

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
            LOGGER.log(Level.SEVERE, "Error generating invoice number", e);
            // Fallback to timestamp-based invoice number if DB query fails
            return prefix + datePart + "-" + System.currentTimeMillis() % 10000;
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));

        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            invoice.setCustomerId(customerId);
            invoice.setCustomerName(rs.getString("customer_name"));
        }

        invoice.setCashierId(rs.getInt("cashier_id"));
        invoice.setCashierName(rs.getString("cashier_name"));
        invoice.setInvoiceDate(rs.getTimestamp("invoice_date"));
        invoice.setSubtotal(rs.getBigDecimal("subtotal"));
        invoice.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        invoice.setTaxAmount(rs.getBigDecimal("tax_amount"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setPaymentMethod(rs.getString("payment_method"));
        invoice.setNotes(rs.getString("notes"));
        invoice.setCreatedAt(rs.getTimestamp("created_at"));

        return invoice;
    }
}
