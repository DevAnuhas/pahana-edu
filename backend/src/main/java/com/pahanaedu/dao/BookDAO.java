package com.pahanaedu.dao;

import com.pahanaedu.model.Book;
import com.pahanaedu.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Book-related database operations
 */
public class BookDAO {
    private static final Logger LOGGER = Logger.getLogger(BookDAO.class.getName());

    public Book findById(int id) {
        Book book = null;
        String sql = "SELECT b.*, c.name AS category_name, p.name AS publisher_name " +
                     "FROM books b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "JOIN publishers p ON b.publisher_id = p.id " +
                     "WHERE b.id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    book = mapResultSetToBook(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding book by id: " + id, e);
        }

        return book;
    }

    public Book findByIsbn(String isbn) {
        Book book = null;
        String sql = "SELECT b.*, c.name AS category_name, p.name AS publisher_name " +
                     "FROM books b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "JOIN publishers p ON b.publisher_id = p.id " +
                     "WHERE b.isbn = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    book = mapResultSetToBook(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding book by ISBN: " + isbn, e);
        }

        return book;
    }

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.name AS category_name, p.name AS publisher_name " +
                     "FROM books b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "JOIN publishers p ON b.publisher_id = p.id " +
                     "ORDER BY b.title";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = mapResultSetToBook(rs);
                books.add(book);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all books", e);
        }

        return books;
    }

    public List<Book> findByCategory(int categoryId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.name AS category_name, p.name AS publisher_name " +
                     "FROM books b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "JOIN publishers p ON b.publisher_id = p.id " +
                     "WHERE b.category_id = ? " +
                     "ORDER BY b.title";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = mapResultSetToBook(rs);
                    books.add(book);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding books by category ID: " + categoryId, e);
        }

        return books;
    }

    public List<Book> searchBooks(String searchTerm) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.name AS category_name, p.name AS publisher_name " +
                     "FROM books b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "JOIN publishers p ON b.publisher_id = p.id " +
                     "WHERE b.isbn LIKE ? OR b.title LIKE ? OR b.author LIKE ? " +
                     "ORDER BY b.title";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = mapResultSetToBook(rs);
                    books.add(book);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching books with term: " + searchTerm, e);
        }

        return books;
    }

    public boolean create(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category_id, publisher_id, " +
                     "publication_year, price, stock_quantity, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setInt(4, book.getCategoryId());
            stmt.setInt(5, book.getPublisherId());

            if (book.getPublicationYear() != null) {
                stmt.setInt(6, book.getPublicationYear());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            stmt.setBigDecimal(7, book.getPrice());
            stmt.setInt(8, book.getStockQuantity());
            stmt.setString(9, book.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        book.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating book: " + book.getTitle(), e);
        }

        return false;
    }

    public boolean update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, category_id = ?, " +
                     "publisher_id = ?, publication_year = ?, price = ?, " +
                     "stock_quantity = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getCategoryId());
            stmt.setInt(4, book.getPublisherId());

            if (book.getPublicationYear() != null) {
                stmt.setInt(5, book.getPublicationYear());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            stmt.setBigDecimal(6, book.getPrice());
            stmt.setInt(7, book.getStockQuantity());
            stmt.setString(8, book.getDescription());
            stmt.setInt(9, book.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book with ID: " + book.getId(), e);
            return false;
        }
    }

    public boolean updateStock(int bookId, int quantityChange) {
        String sql = "UPDATE books SET stock_quantity = stock_quantity + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityChange);
            stmt.setInt(2, bookId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating stock for book ID: " + bookId, e);
            return false;
        }
    }

    public boolean delete(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting book with ID: " + bookId, e);
            return false;
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setPublisherId(rs.getInt("publisher_id"));

        try {
            book.setPublicationYear(rs.getInt("publication_year"));
            if (rs.wasNull()) {
                book.setPublicationYear(null);
            }
        } catch (SQLException e) {
            book.setPublicationYear(null);
        }

        book.setPrice(rs.getBigDecimal("price"));
        book.setStockQuantity(rs.getInt("stock_quantity"));
        book.setDescription(rs.getString("description"));
        book.setCreatedAt(rs.getTimestamp("created_at"));
        book.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Get joined data if available
        try {
            book.setCategoryName(rs.getString("category_name"));
            book.setPublisherName(rs.getString("publisher_name"));
        } catch (SQLException e) {
            // These columns may not be in the result set, which is fine
        }

        return book;
    }
}
