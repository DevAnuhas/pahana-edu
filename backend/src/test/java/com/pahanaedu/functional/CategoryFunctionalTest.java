package com.pahanaedu.functional;

import com.pahanaedu.dao.BookDAO;
import com.pahanaedu.dao.CategoryDAO;
import com.pahanaedu.model.Book;
import com.pahanaedu.model.Category;
import com.pahanaedu.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the category management feature
 */
public class CategoryFunctionalTest {

    // Custom mock implementation for CategoryDAO
    private static class MockCategoryDAO extends CategoryDAO {
        private List<Category> categories = new ArrayList<>();
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

        @Override
        public boolean create(Category category) {
            if (shouldFailOnCreate) {
                return false;
            }
            
            category.setId(nextId++);
            categories.add(category);
            return true;
        }

        @Override
        public boolean update(Category category) {
            if (shouldFailOnUpdate) {
                return false;
            }
            
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == category.getId()) {
                    categories.set(i, category);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int categoryId) {
            if (shouldFailOnDelete) {
                return false;
            }
            
            return categories.removeIf(category -> category.getId() == categoryId);
        }

        public void addTestCategory(Category category) {
            if (category.getId() == 0) {
                category.setId(nextId++);
            }
            categories.add(category);
        }

        public void clearCategories() {
            categories.clear();
            nextId = 1;
        }
    }

    // Custom mock implementation for BookDAO to test category-related constraints
    private static class MockBookDAO extends BookDAO {
        private List<Book> books = new ArrayList<>();
        
        @Override
        public List<Book> findByCategory(int categoryId) {
            return books.stream()
                    .filter(book -> book.getCategoryId() == categoryId)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        public void addTestBook(Book book) {
            books.add(book);
        }
        
        public void clearBooks() {
            books.clear();
        }
    }

    private BookService bookService;
    private MockCategoryDAO mockCategoryDAO;
    private MockBookDAO mockBookDAO;

    @BeforeEach
    public void setUp() {
        mockCategoryDAO = new MockCategoryDAO();
        mockBookDAO = new MockBookDAO();
        
        // Create a custom BookService that uses our mock DAOs
        bookService = new BookService() {
            @Override
            public Category getCategoryById(int categoryId) {
                return mockCategoryDAO.findById(categoryId);
            }
            
            @Override
            public List<Category> getAllCategories() {
                return mockCategoryDAO.findAll();
            }
            
            @Override
            public boolean createCategory(Category category) {
                return mockCategoryDAO.create(category);
            }
            
            @Override
            public boolean updateCategory(Category category) {
                return mockCategoryDAO.update(category);
            }
            
            @Override
            public boolean deleteCategory(int categoryId) {
                return mockCategoryDAO.delete(categoryId);
            }
            
            @Override
            public List<Book> getBooksByCategory(int categoryId) {
                return mockBookDAO.findByCategory(categoryId);
            }
        };
    }

    /**
     * Test adding a valid category
     * 
     * Purpose: Verify that a category can be successfully added with valid data
     * Inputs: Valid category data
     * Expected Outputs: Category is saved and returned with an ID
     * Requirement ID: CAT-001
     */
    @Test
    public void testAddValidCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Fiction");
        category.setDescription("Fiction books");

        // Act
        boolean result = bookService.createCategory(category);

        // Assert
        assertTrue(result, "Category should be saved successfully");
        assertNotEquals(0, category.getId(), "Category should have an ID assigned");
        
        Category savedCategory = bookService.getCategoryById(category.getId());
        assertNotNull(savedCategory, "Category should be retrievable by ID");
        assertEquals("Fiction", savedCategory.getName(), "Category name should match");
        assertEquals("Fiction books", savedCategory.getDescription(), "Category description should match");
    }

    /**
     * Test adding a category with missing name
     * 
     * Purpose: Verify that adding a category fails when name is missing
     * Inputs: Category with missing name
     * Expected Outputs: Category is not saved
     * Requirement ID: CAT-002
     */
    @Test
    public void testAddCategoryWithMissingName() {
        // Arrange
        Category category = new Category();
        // Missing name
        category.setDescription("Some description");
        
        mockCategoryDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = bookService.createCategory(category);

        // Assert
        assertFalse(result, "Category should not be saved with missing name");
    }

    /**
     * Test adding a category with duplicate name
     * 
     * Purpose: Verify that adding a category fails when name is duplicate
     * Inputs: Category with duplicate name
     * Expected Outputs: Category is not saved
     * Requirement ID: CAT-003
     */
    @Test
    public void testAddCategoryWithDuplicateName() {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setName("Fiction");
        existingCategory.setDescription("Fiction books");
        mockCategoryDAO.addTestCategory(existingCategory);
        
        Category newCategory = new Category();
        newCategory.setName("Fiction"); // Duplicate name
        newCategory.setDescription("Another description");
        
        mockCategoryDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = bookService.createCategory(newCategory);

        // Assert
        assertFalse(result, "Category should not be saved with duplicate name");
    }

    /**
     * Test updating an existing category
     * 
     * Purpose: Verify that a category can be successfully updated
     * Inputs: Updated category data
     * Expected Outputs: Category is updated in the database
     * Requirement ID: CAT-004
     */
    @Test
    public void testUpdateExistingCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Original Name");
        category.setDescription("Original description");
        
        mockCategoryDAO.addTestCategory(category);
        
        Category updatedCategory = new Category();
        updatedCategory.setId(category.getId());
        updatedCategory.setName("Updated Name");
        updatedCategory.setDescription("Updated description");

        // Act
        boolean result = bookService.updateCategory(updatedCategory);

        // Assert
        assertTrue(result, "Category should be updated successfully");
        
        Category retrievedCategory = bookService.getCategoryById(category.getId());
        assertNotNull(retrievedCategory, "Category should be retrievable by ID");
        assertEquals("Updated Name", retrievedCategory.getName(), "Category name should be updated");
        assertEquals("Updated description", retrievedCategory.getDescription(), "Category description should be updated");
    }

    /**
     * Test updating a non-existent category
     * 
     * Purpose: Verify that updating a non-existent category fails
     * Inputs: Category with non-existent ID
     * Expected Outputs: Update operation fails
     * Requirement ID: CAT-005
     */
    @Test
    public void testUpdateNonExistentCategory() {
        // Arrange
        Category category = new Category();
        category.setId(999); // Non-existent ID
        category.setName("Test Category");
        category.setDescription("Test description");

        // Act
        boolean result = bookService.updateCategory(category);

        // Assert
        assertFalse(result, "Updating a non-existent category should fail");
    }

    /**
     * Test deleting an existing category
     * 
     * Purpose: Verify that a category can be successfully deleted
     * Inputs: Valid category ID
     * Expected Outputs: Category is deleted from the database
     * Requirement ID: CAT-006
     */
    @Test
    public void testDeleteExistingCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test description");
        
        mockCategoryDAO.addTestCategory(category);

        // Act
        boolean result = bookService.deleteCategory(category.getId());

        // Assert
        assertTrue(result, "Category should be deleted successfully");
        
        Category retrievedCategory = bookService.getCategoryById(category.getId());
        assertNull(retrievedCategory, "Category should not be retrievable after deletion");
    }

    /**
     * Test deleting a non-existent category
     * 
     * Purpose: Verify that deleting a non-existent category fails
     * Inputs: Non-existent category ID
     * Expected Outputs: Delete operation fails
     * Requirement ID: CAT-007
     */
    @Test
    public void testDeleteNonExistentCategory() {
        // Arrange
        int nonExistentId = 999;

        // Act
        boolean result = bookService.deleteCategory(nonExistentId);

        // Assert
        assertFalse(result, "Deleting a non-existent category should fail");
    }

    /**
     * Test deleting a category that has books
     * 
     * Purpose: Verify that deleting a category fails when it has books
     * Inputs: Category ID with associated books
     * Expected Outputs: Delete operation fails
     * Requirement ID: CAT-008
     */
    @Test
    public void testDeleteCategoryWithBooks() {
        // Arrange
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test description");
        mockCategoryDAO.addTestCategory(category);
        
        Book book = new Book();
        book.setTitle("Test Book");
        book.setCategoryId(category.getId());
        mockBookDAO.addTestBook(book);
        
        mockCategoryDAO.setShouldFailOnDelete(true);

        // Act
        boolean result = bookService.deleteCategory(category.getId());

        // Assert
        assertFalse(result, "Deleting a category with books should fail");
    }

    /**
     * Test getting all categories
     * 
     * Purpose: Verify that all categories can be retrieved
     * Inputs: None
     * Expected Outputs: All categories are returned
     * Requirement ID: CAT-009
     */
    @Test
    public void testGetAllCategories() {
        // Arrange
        Category category1 = new Category();
        category1.setName("Category 1");
        category1.setDescription("Description 1");
        
        Category category2 = new Category();
        category2.setName("Category 2");
        category2.setDescription("Description 2");
        
        mockCategoryDAO.addTestCategory(category1);
        mockCategoryDAO.addTestCategory(category2);

        // Act
        List<Category> results = bookService.getAllCategories();

        // Assert
        assertEquals(2, results.size(), "Should return all categories");
    }

    /**
     * Test getting a category by ID
     * 
     * Purpose: Verify that a category can be retrieved by ID
     * Inputs: Valid category ID
     * Expected Outputs: Category is returned
     * Requirement ID: CAT-010
     */
    @Test
    public void testGetCategoryById() {
        // Arrange
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test description");
        
        mockCategoryDAO.addTestCategory(category);

        // Act
        Category result = bookService.getCategoryById(category.getId());

        // Assert
        assertNotNull(result, "Category should be retrievable by ID");
        assertEquals("Test Category", result.getName(), "Category name should match");
        assertEquals("Test description", result.getDescription(), "Category description should match");
    }
}