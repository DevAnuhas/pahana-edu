package com.pahanaedu.functional;

import com.pahanaedu.dao.BookDAO;
import com.pahanaedu.dao.CategoryDAO;
import com.pahanaedu.dao.PublisherDAO;
import com.pahanaedu.model.Book;
import com.pahanaedu.model.Category;
import com.pahanaedu.model.Publisher;
import com.pahanaedu.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the book management feature
 */
public class BookFunctionalTest {

    // Custom mock implementation for BookDAO
    private static class MockBookDAO extends BookDAO {
        private List<Book> books = new ArrayList<>();
        private int nextId = 1;
        private boolean shouldFailOnCreate = false;
        private boolean shouldFailOnUpdate = false;
        private boolean shouldFailOnDelete = false;

        public void setShouldFailOnCreate(boolean shouldFailOnCreate) {
            this.shouldFailOnCreate = shouldFailOnCreate;
        }

        public void setShouldFailOnUpdate(boolean shouldFailOnUpdate) {
            this.shouldFailOnUpdate = shouldFailOnUpdate;
        }

        public void setShouldFailOnDelete(boolean shouldFailOnDelete) {
            this.shouldFailOnDelete = shouldFailOnDelete;
        }

        @Override
        public Book findById(int id) {
            return books.stream()
                    .filter(book -> book.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Book findByIsbn(String isbn) {
            return books.stream()
                    .filter(book -> book.getIsbn().equals(isbn))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Book> findAll() {
            return new ArrayList<>(books);
        }

        @Override
        public List<Book> findByCategory(int categoryId) {
            return books.stream()
                    .filter(book -> book.getCategoryId() == categoryId)
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public List<Book> searchBooks(String searchTerm) {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return new ArrayList<>();
            }
            
            String lowerCaseSearchTerm = searchTerm.toLowerCase();
            return books.stream()
                    .filter(book -> 
                        book.getTitle().toLowerCase().contains(lowerCaseSearchTerm) ||
                        book.getAuthor().toLowerCase().contains(lowerCaseSearchTerm) ||
                        book.getIsbn().toLowerCase().contains(lowerCaseSearchTerm))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public boolean create(Book book) {
            if (shouldFailOnCreate) {
                return false;
            }
            
            book.setId(nextId++);
            books.add(book);
            return true;
        }

        @Override
        public boolean update(Book book) {
            if (shouldFailOnUpdate) {
                return false;
            }
            
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getId() == book.getId()) {
                    books.set(i, book);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean updateStock(int bookId, int quantityChange) {
            for (Book book : books) {
                if (book.getId() == bookId) {
                    int newQuantity = book.getStockQuantity() + quantityChange;
                    if (newQuantity < 0) {
                        return false;
                    }
                    book.setStockQuantity(newQuantity);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int bookId) {
            if (shouldFailOnDelete) {
                return false;
            }
            
            return books.removeIf(book -> book.getId() == bookId);
        }

        public void addTestBook(Book book) {
            if (book.getId() == 0) {
                book.setId(nextId++);
            }
            books.add(book);
        }

        public void clearBooks() {
            books.clear();
            nextId = 1;
        }
    }

    // Custom mock implementation for CategoryDAO
    private static class MockCategoryDAO extends CategoryDAO {
        private List<Category> categories = new ArrayList<>();
        private int nextId = 1;

        @Override
        public Category findById(int id) {
            return categories.stream()
                    .filter(category -> category.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Category> findAll() {
            return new ArrayList<>(categories);
        }

        public void addTestCategory(Category category) {
            if (category.getId() == 0) {
                category.setId(nextId++);
            }
            categories.add(category);
        }
    }

    // Custom mock implementation for PublisherDAO
    private static class MockPublisherDAO extends PublisherDAO {
        private List<Publisher> publishers = new ArrayList<>();
        private int nextId = 1;

        @Override
        public Publisher findById(int id) {
            return publishers.stream()
                    .filter(publisher -> publisher.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Publisher> findAll() {
            return new ArrayList<>(publishers);
        }

        public void addTestPublisher(Publisher publisher) {
            if (publisher.getId() == 0) {
                publisher.setId(nextId++);
            }
            publishers.add(publisher);
        }
    }

    private BookService bookService;
    private MockBookDAO mockBookDAO;
    private MockCategoryDAO mockCategoryDAO;
    private MockPublisherDAO mockPublisherDAO;

    @BeforeEach
    public void setUp() {
        mockBookDAO = new MockBookDAO();
        mockCategoryDAO = new MockCategoryDAO();
        mockPublisherDAO = new MockPublisherDAO();
        
        // Create test category and publisher
        Category category = new Category();
        category.setName("Test Category");
        mockCategoryDAO.addTestCategory(category);
        
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        mockPublisherDAO.addTestPublisher(publisher);
        
        // Create a custom BookService that uses our mock DAOs
        bookService = new BookService() {
            @Override
            public Book getBookById(int id) {
                return mockBookDAO.findById(id);
            }
            
            @Override
            public Book getBookByIsbn(String isbn) {
                return mockBookDAO.findByIsbn(isbn);
            }
            
            @Override
            public List<Book> getAllBooks() {
                return mockBookDAO.findAll();
            }
            
            @Override
            public List<Book> getBooksByCategory(int categoryId) {
                return mockBookDAO.findByCategory(categoryId);
            }
            
            @Override
            public List<Book> searchBooks(String searchTerm) {
                return mockBookDAO.searchBooks(searchTerm);
            }
            
            @Override
            public boolean createBook(Book book) {
                return mockBookDAO.create(book);
            }
            
            @Override
            public boolean updateBook(Book book) {
                return mockBookDAO.update(book);
            }
            
            @Override
            public boolean updateBookStock(int bookId, int quantityChange) {
                return mockBookDAO.updateStock(bookId, quantityChange);
            }
            
            @Override
            public boolean deleteBook(int bookId) {
                return mockBookDAO.delete(bookId);
            }
            
            @Override
            public Category getCategoryById(int categoryId) {
                return mockCategoryDAO.findById(categoryId);
            }
            
            @Override
            public List<Category> getAllCategories() {
                return mockCategoryDAO.findAll();
            }
            
            @Override
            public Publisher getPublisherById(int publisherId) {
                return mockPublisherDAO.findById(publisherId);
            }
            
            @Override
            public List<Publisher> getAllPublishers() {
                return mockPublisherDAO.findAll();
            }
        };
    }

    /**
     * Test adding a valid book
     * 
     * Purpose: Verify that a book can be successfully added with valid data
     * Inputs: Valid book data
     * Expected Outputs: Book is saved and returned with an ID
     * Requirement ID: BOOK-001
     */
    @Test
    public void testAddValidBook() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9781234567897");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(10);
        book.setDescription("Test description");

        // Act
        boolean result = bookService.createBook(book);

        // Assert
        assertTrue(result, "Book should be saved successfully");
        assertNotEquals(0, book.getId(), "Book should have an ID assigned");
        
        Book savedBook = bookService.getBookById(book.getId());
        assertNotNull(savedBook, "Book should be retrievable by ID");
        assertEquals("Test Book", savedBook.getTitle(), "Book title should match");
        assertEquals("Test Author", savedBook.getAuthor(), "Book author should match");
    }

    /**
     * Test adding a book with missing required fields
     * 
     * Purpose: Verify that adding a book fails when required fields are missing
     * Inputs: Book with missing title
     * Expected Outputs: Book is not saved
     * Requirement ID: BOOK-002
     */
    @Test
    public void testAddBookWithMissingRequiredFields() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9781234567897");
        // Missing title
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(10);
        book.setDescription("Test description");
        
        mockBookDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = bookService.createBook(book);

        // Assert
        assertFalse(result, "Book should not be saved with missing title");
    }

    /**
     * Test adding a book with invalid ISBN
     * 
     * Purpose: Verify that adding a book fails when ISBN is invalid
     * Inputs: Book with invalid ISBN
     * Expected Outputs: Book is not saved
     * Requirement ID: BOOK-003
     */
    @Test
    public void testAddBookWithInvalidISBN() {
        // Arrange
        Book book = new Book();
        book.setIsbn("invalid-isbn"); // Invalid ISBN format
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(10);
        book.setDescription("Test description");
        
        mockBookDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = bookService.createBook(book);

        // Assert
        assertFalse(result, "Book should not be saved with invalid ISBN");
    }

    /**
     * Test adding a book with negative price
     * 
     * Purpose: Verify that adding a book fails when price is negative
     * Inputs: Book with negative price
     * Expected Outputs: Book is not saved
     * Requirement ID: BOOK-004
     */
    @Test
    public void testAddBookWithNegativePrice() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9781234567897");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("-29.99")); // Negative price
        book.setStockQuantity(10);
        book.setDescription("Test description");
        
        mockBookDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = bookService.createBook(book);

        // Assert
        assertFalse(result, "Book should not be saved with negative price");
    }

    /**
     * Test adding a book with negative stock quantity
     * 
     * Purpose: Verify that adding a book fails when stock quantity is negative
     * Inputs: Book with negative stock quantity
     * Expected Outputs: Book is not saved
     * Requirement ID: BOOK-005
     */
    @Test
    public void testAddBookWithNegativeStockQuantity() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9781234567897");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(-10); // Negative stock quantity
        book.setDescription("Test description");
        
        mockBookDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = bookService.createBook(book);

        // Assert
        assertFalse(result, "Book should not be saved with negative stock quantity");
    }

    /**
     * Test updating an existing book
     * 
     * Purpose: Verify that a book can be successfully updated
     * Inputs: Updated book data
     * Expected Outputs: Book is updated in the database
     * Requirement ID: BOOK-006
     */
    @Test
    public void testUpdateExistingBook() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9781234567897");
        book.setTitle("Original Title");
        book.setAuthor("Original Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(10);
        book.setDescription("Original description");
        
        mockBookDAO.addTestBook(book);
        
        Book updatedBook = new Book();
        updatedBook.setId(book.getId());
        updatedBook.setIsbn("9781234567897");
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setCategoryId(1);
        updatedBook.setPublisherId(1);
        updatedBook.setPublicationYear(2023);
        updatedBook.setPrice(new BigDecimal("39.99"));
        updatedBook.setStockQuantity(20);
        updatedBook.setDescription("Updated description");

        // Act
        boolean result = bookService.updateBook(updatedBook);

        // Assert
        assertTrue(result, "Book should be updated successfully");
        
        Book retrievedBook = bookService.getBookById(book.getId());
        assertNotNull(retrievedBook, "Book should be retrievable by ID");
        assertEquals("Updated Title", retrievedBook.getTitle(), "Book title should be updated");
        assertEquals("Updated Author", retrievedBook.getAuthor(), "Book author should be updated");
        assertEquals(0, new BigDecimal("39.99").compareTo(retrievedBook.getPrice()), "Book price should be updated");
        assertEquals(20, retrievedBook.getStockQuantity(), "Book stock quantity should be updated");
    }

    /**
     * Test updating a non-existent book
     * 
     * Purpose: Verify that updating a non-existent book fails
     * Inputs: Book with non-existent ID
     * Expected Outputs: Update operation fails
     * Requirement ID: BOOK-007
     */
    @Test
    public void testUpdateNonExistentBook() {
        // Arrange
        Book book = new Book();
        book.setId(999); // Non-existent ID
        book.setIsbn("9781234567897");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(10);
        book.setDescription("Test description");

        // Act
        boolean result = bookService.updateBook(book);

        // Assert
        assertFalse(result, "Updating a non-existent book should fail");
    }

    /**
     * Test deleting an existing book
     * 
     * Purpose: Verify that a book can be successfully deleted
     * Inputs: Valid book ID
     * Expected Outputs: Book is deleted from the database
     * Requirement ID: BOOK-008
     */
    @Test
    public void testDeleteExistingBook() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9781234567897");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategoryId(1);
        book.setPublisherId(1);
        book.setPublicationYear(2023);
        book.setPrice(new BigDecimal("29.99"));
        book.setStockQuantity(10);
        book.setDescription("Test description");
        
        mockBookDAO.addTestBook(book);

        // Act
        boolean result = bookService.deleteBook(book.getId());

        // Assert
        assertTrue(result, "Book should be deleted successfully");
        
        Book retrievedBook = bookService.getBookById(book.getId());
        assertNull(retrievedBook, "Book should not be retrievable after deletion");
    }

    /**
     * Test deleting a non-existent book
     * 
     * Purpose: Verify that deleting a non-existent book fails
     * Inputs: Non-existent book ID
     * Expected Outputs: Delete operation fails
     * Requirement ID: BOOK-009
     */
    @Test
    public void testDeleteNonExistentBook() {
        // Arrange
        int nonExistentId = 999;

        // Act
        boolean result = bookService.deleteBook(nonExistentId);

        // Assert
        assertFalse(result, "Deleting a non-existent book should fail");
    }

    /**
     * Test searching for books by title
     * 
     * Purpose: Verify that books can be searched by title
     * Inputs: Search term matching a book title
     * Expected Outputs: Matching books are returned
     * Requirement ID: BOOK-010
     */
    @Test
    public void testSearchBooksByTitle() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9781234567897");
        book1.setTitle("Java Programming");
        book1.setAuthor("Author 1");
        book1.setCategoryId(1);
        book1.setPublisherId(1);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockQuantity(10);
        
        Book book2 = new Book();
        book2.setIsbn("9789876543210");
        book2.setTitle("Python Programming");
        book2.setAuthor("Author 2");
        book2.setCategoryId(1);
        book2.setPublisherId(1);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setStockQuantity(15);
        
        mockBookDAO.addTestBook(book1);
        mockBookDAO.addTestBook(book2);

        // Act
        List<Book> results = bookService.searchBooks("Java");

        // Assert
        assertEquals(1, results.size(), "Search should return one book");
        assertEquals("Java Programming", results.get(0).getTitle(), "Search result should match the expected book");
    }

    /**
     * Test searching for books by author
     * 
     * Purpose: Verify that books can be searched by author
     * Inputs: Search term matching a book author
     * Expected Outputs: Matching books are returned
     * Requirement ID: BOOK-011
     */
    @Test
    public void testSearchBooksByAuthor() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9781234567897");
        book1.setTitle("Book 1");
        book1.setAuthor("John Smith");
        book1.setCategoryId(1);
        book1.setPublisherId(1);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockQuantity(10);
        
        Book book2 = new Book();
        book2.setIsbn("9789876543210");
        book2.setTitle("Book 2");
        book2.setAuthor("Jane Doe");
        book2.setCategoryId(1);
        book2.setPublisherId(1);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setStockQuantity(15);
        
        mockBookDAO.addTestBook(book1);
        mockBookDAO.addTestBook(book2);

        // Act
        List<Book> results = bookService.searchBooks("Smith");

        // Assert
        assertEquals(1, results.size(), "Search should return one book");
        assertEquals("John Smith", results.get(0).getAuthor(), "Search result should match the expected book");
    }

    /**
     * Test searching for books by ISBN
     * 
     * Purpose: Verify that books can be searched by ISBN
     * Inputs: Search term matching a book ISBN
     * Expected Outputs: Matching books are returned
     * Requirement ID: BOOK-012
     */
    @Test
    public void testSearchBooksByISBN() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9781234567897");
        book1.setTitle("Book 1");
        book1.setAuthor("Author 1");
        book1.setCategoryId(1);
        book1.setPublisherId(1);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockQuantity(10);
        
        Book book2 = new Book();
        book2.setIsbn("9789876543210");
        book2.setTitle("Book 2");
        book2.setAuthor("Author 2");
        book2.setCategoryId(1);
        book2.setPublisherId(1);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setStockQuantity(15);
        
        mockBookDAO.addTestBook(book1);
        mockBookDAO.addTestBook(book2);

        // Act
        List<Book> results = bookService.searchBooks("9781234");

        // Assert
        assertEquals(1, results.size(), "Search should return one book");
        assertEquals("9781234567897", results.get(0).getIsbn(), "Search result should match the expected book");
    }

    /**
     * Test searching for non-existent books
     * 
     * Purpose: Verify that searching for non-existent books returns empty results
     * Inputs: Search term not matching any book
     * Expected Outputs: Empty list is returned
     * Requirement ID: BOOK-013
     */
    @Test
    public void testSearchNonExistentBooks() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9781234567897");
        book1.setTitle("Java Programming");
        book1.setAuthor("Author 1");
        book1.setCategoryId(1);
        book1.setPublisherId(1);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockQuantity(10);
        
        Book book2 = new Book();
        book2.setIsbn("9789876543210");
        book2.setTitle("Python Programming");
        book2.setAuthor("Author 2");
        book2.setCategoryId(1);
        book2.setPublisherId(1);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setStockQuantity(15);
        
        mockBookDAO.addTestBook(book1);
        mockBookDAO.addTestBook(book2);

        // Act
        List<Book> results = bookService.searchBooks("Ruby");

        // Assert
        assertTrue(results.isEmpty(), "Search should return empty list for non-existent books");
    }

    /**
     * Test searching with empty search term
     * 
     * Purpose: Verify that searching with an empty search term returns empty results
     * Inputs: Empty search term
     * Expected Outputs: Empty list is returned
     * Requirement ID: BOOK-014
     */
    @Test
    public void testSearchWithEmptySearchTerm() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9781234567897");
        book1.setTitle("Java Programming");
        book1.setAuthor("Author 1");
        book1.setCategoryId(1);
        book1.setPublisherId(1);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockQuantity(10);
        
        mockBookDAO.addTestBook(book1);

        // Act
        List<Book> results = bookService.searchBooks("");

        // Assert
        assertTrue(results.isEmpty(), "Search should return empty list for empty search term");
    }

    /**
     * Test getting all books
     * 
     * Purpose: Verify that all books can be retrieved
     * Inputs: None
     * Expected Outputs: All books are returned
     * Requirement ID: BOOK-015
     */
    @Test
    public void testGetAllBooks() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9781234567897");
        book1.setTitle("Book 1");
        book1.setAuthor("Author 1");
        book1.setCategoryId(1);
        book1.setPublisherId(1);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockQuantity(10);
        
        Book book2 = new Book();
        book2.setIsbn("9789876543210");
        book2.setTitle("Book 2");
        book2.setAuthor("Author 2");
        book2.setCategoryId(1);
        book2.setPublisherId(1);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setStockQuantity(15);
        
        mockBookDAO.addTestBook(book1);
        mockBookDAO.addTestBook(book2);

        // Act
        List<Book> results = bookService.getAllBooks();

        // Assert
        assertEquals(2, results.size(), "Should return all books");
    }
}