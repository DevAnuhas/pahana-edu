package com.pahanaedu.service;

import com.pahanaedu.dao.BookDAO;
import com.pahanaedu.dao.CategoryDAO;
import com.pahanaedu.dao.PublisherDAO;
import com.pahanaedu.model.Book;
import com.pahanaedu.model.Category;
import com.pahanaedu.model.Publisher;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for handling book-related business logic
 */
public class BookService {
    private static final Logger LOGGER = Logger.getLogger(BookService.class.getName());
    private final BookDAO bookDAO;
    private final CategoryDAO categoryDAO;
    private final PublisherDAO publisherDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
        this.categoryDAO = new CategoryDAO();
        this.publisherDAO = new PublisherDAO();
    }

    public Book getBookById(int id) {
        LOGGER.info("Fetching book with ID: " + id);
        return bookDAO.findById(id);
    }

    public Book getBookByIsbn(String isbn) {
        LOGGER.info("Fetching book with ISBN: " + isbn);
        return bookDAO.findByIsbn(isbn);
    }

    public List<Book> getAllBooks() {
        LOGGER.info("Fetching all books");
        return bookDAO.findAll();
    }

    public List<Book> getBooksByCategory(int categoryId) {
        LOGGER.info("Fetching books for category ID: " + categoryId);
        return bookDAO.findByCategory(categoryId);
    }

    public List<Book> searchBooks(String searchTerm) {
        LOGGER.info("Searching books with term: " + searchTerm);
        return bookDAO.searchBooks(searchTerm);
    }

    public boolean createBook(Book book) {
        LOGGER.info("Creating new book: " + book.getTitle());

        // Validate category exists
        Category category = categoryDAO.findById(book.getCategoryId());
        if (category == null) {
            LOGGER.warning("Invalid category ID: " + book.getCategoryId());
            return false;
        }

        // Validate publisher exists
        Publisher publisher = publisherDAO.findById(book.getPublisherId());
        if (publisher == null) {
            LOGGER.warning("Invalid publisher ID: " + book.getPublisherId());
            return false;
        }

        return bookDAO.create(book);
    }

    public boolean updateBook(Book book) {
        LOGGER.info("Updating book with ID: " + book.getId());

        // Validate category exists
        Category category = categoryDAO.findById(book.getCategoryId());
        if (category == null) {
            LOGGER.warning("Invalid category ID: " + book.getCategoryId());
            return false;
        }

        // Validate publisher exists
        Publisher publisher = publisherDAO.findById(book.getPublisherId());
        if (publisher == null) {
            LOGGER.warning("Invalid publisher ID: " + book.getPublisherId());
            return false;
        }

        return bookDAO.update(book);
    }

    public boolean updateBookStock(int bookId, int quantityChange) {
        LOGGER.info("Updating stock for book ID: " + bookId + " by " + quantityChange);
        return bookDAO.updateStock(bookId, quantityChange);
    }

    public boolean deleteBook(int bookId) {
        LOGGER.info("Deleting book with ID: " + bookId);
        return bookDAO.delete(bookId);
    }

    public List<Category> getAllCategories() {
        LOGGER.info("Fetching all categories");
        return categoryDAO.findAll();
    }

    public Category getCategoryById(int categoryId) {
        LOGGER.info("Fetching category with ID: " + categoryId);
        return categoryDAO.findById(categoryId);
    }

    public boolean createCategory(Category category) {
        LOGGER.info("Creating new category: " + category.getName());
        return categoryDAO.create(category);
    }

    public boolean updateCategory(Category category) {
        LOGGER.info("Updating category with ID: " + category.getId());
        return categoryDAO.update(category);
    }

    public boolean deleteCategory(int categoryId) {
        LOGGER.info("Deleting category with ID: " + categoryId);
        return categoryDAO.delete(categoryId);
    }

    public List<Publisher> getAllPublishers() {
        LOGGER.info("Fetching all publishers");
        return publisherDAO.findAll();
    }

    public Publisher getPublisherById(int publisherId) {
        LOGGER.info("Fetching publisher with ID: " + publisherId);
        return publisherDAO.findById(publisherId);
    }

    public boolean createPublisher(Publisher publisher) {
        LOGGER.info("Creating new publisher: " + publisher.getName());
        return publisherDAO.create(publisher);
    }

    public boolean updatePublisher(Publisher publisher) {
        LOGGER.info("Updating publisher with ID: " + publisher.getId());
        return publisherDAO.update(publisher);
    }

    public boolean deletePublisher(int publisherId) {
        LOGGER.info("Deleting publisher with ID: " + publisherId);
        return publisherDAO.delete(publisherId);
    }
}
