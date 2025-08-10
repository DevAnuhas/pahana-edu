package com.pahanaedu.dao;

import com.pahanaedu.model.InvoiceItem;
import com.pahanaedu.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for InvoiceItem-related database operations
 */
public class InvoiceItemDAO {
    private static final Logger LOGGER = Logger.getLogger(InvoiceItemDAO.class.getName());

    public List<InvoiceItem> findByInvoiceId(int invoiceId) {
        List<InvoiceItem> items = new ArrayList<>();
        // Use explicit column names and avoid * to prevent any ambiguity
        String sql = "SELECT DISTINCT i.id, i.invoice_id, i.book_id, i.quantity, i.unit_price, " +
                     "i.discount_percent, i.total_price, i.created_at, " +
                     "b.title AS book_title, b.isbn AS book_isbn " +
                     "FROM invoice_items i " +
                     "JOIN books b ON i.book_id = b.id " +
                     "WHERE i.invoice_id = ? " +
                     "ORDER BY i.id"; // Add ordering to ensure consistent results

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            LOGGER.info("Executing query for invoice items with invoice ID: " + invoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = mapResultSetToInvoiceItem(rs);
                    items.add(item);
                }
            }

            LOGGER.info("Found " + items.size() + " items for invoice ID: " + invoiceId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice items by invoice ID: " + invoiceId, e);
        }

        return items;
    }

    public boolean create(InvoiceItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO invoice_items (invoice_id, book_id, quantity, unit_price, " +
                     "discount_percent, total_price) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getInvoiceId());
            stmt.setInt(2, item.getBookId());
            stmt.setInt(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getUnitPrice());
            stmt.setBigDecimal(5, item.getDiscountPercent());
            stmt.setBigDecimal(6, item.getTotalPrice());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean create(InvoiceItem item) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return create(item, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating invoice item", e);
            return false;
        }
    }

    private InvoiceItem mapResultSetToInvoiceItem(ResultSet rs) throws SQLException {
        InvoiceItem item = new InvoiceItem();
        item.setId(rs.getInt("id"));
        item.setInvoiceId(rs.getInt("invoice_id"));
        item.setBookId(rs.getInt("book_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        item.setTotalPrice(rs.getBigDecimal("total_price"));
        item.setCreatedAt(rs.getTimestamp("created_at"));

        // Get joined data if available
        try {
            item.setBookTitle(rs.getString("book_title"));
            item.setBookIsbn(rs.getString("book_isbn"));
        } catch (SQLException e) {
            // These columns may not be in the result set, which is fine
        }

        return item;
    }
}
