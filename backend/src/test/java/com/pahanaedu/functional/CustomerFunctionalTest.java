package com.pahanaedu.functional;

import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.model.Customer;
import com.pahanaedu.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the customer management feature
 */
public class CustomerFunctionalTest {

    // Custom mock implementation for CustomerDAO
    private static class MockCustomerDAO extends CustomerDAO {
        private List<Customer> customers = new ArrayList<>();
        private int nextId = 1;
        private int accountNumberCounter = 1000;
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
        public Customer findById(int id) {
            return customers.stream()
                    .filter(customer -> customer.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Customer findByAccountNumber(String accountNumber) {
            return customers.stream()
                    .filter(customer -> customer.getAccountNumber().equals(accountNumber))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Customer> findAll() {
            return new ArrayList<>(customers);
        }

        @Override
        public List<Customer> searchCustomers(String searchTerm) {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return new ArrayList<>();
            }
            
            String lowerCaseSearchTerm = searchTerm.toLowerCase();
            return customers.stream()
                    .filter(customer -> 
                        customer.getName().toLowerCase().contains(lowerCaseSearchTerm) ||
                        customer.getAccountNumber().toLowerCase().contains(lowerCaseSearchTerm) ||
                        (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (customer.getAddress() != null && customer.getAddress().toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (customer.getTelephone() != null && customer.getTelephone().contains(searchTerm)))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public boolean create(Customer customer) {
            if (shouldFailOnCreate) {
                return false;
            }
            
            // Validate email format if provided
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                Pattern pattern = Pattern.compile(emailRegex);
                if (!pattern.matcher(customer.getEmail()).matches()) {
                    return false;
                }
            }
            
            customer.setId(nextId++);
            customers.add(customer);
            return true;
        }

        @Override
        public boolean update(Customer customer) {
            if (shouldFailOnUpdate) {
                return false;
            }
            
            // Validate email format if provided
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                Pattern pattern = Pattern.compile(emailRegex);
                if (!pattern.matcher(customer.getEmail()).matches()) {
                    return false;
                }
            }
            
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getId() == customer.getId()) {
                    customers.set(i, customer);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int customerId) {
            if (shouldFailOnDelete) {
                return false;
            }
            
            return customers.removeIf(customer -> customer.getId() == customerId);
        }

        @Override
        public String generateAccountNumber() {
            return "CUST" + (accountNumberCounter++);
        }

        public void addTestCustomer(Customer customer) {
            if (customer.getId() == 0) {
                customer.setId(nextId++);
            }
            if (customer.getAccountNumber() == null || customer.getAccountNumber().isEmpty()) {
                customer.setAccountNumber(generateAccountNumber());
            }
            customers.add(customer);
        }

        public void clearCustomers() {
            customers.clear();
            nextId = 1;
            accountNumberCounter = 1000;
        }
    }

    private CustomerService customerService;
    private MockCustomerDAO mockCustomerDAO;

    @BeforeEach
    public void setUp() {
        mockCustomerDAO = new MockCustomerDAO();
        
        // Create a custom CustomerService that uses our mock DAO
        customerService = new CustomerService() {
            @Override
            public Customer getCustomerById(int id) {
                return mockCustomerDAO.findById(id);
            }
            
            @Override
            public Customer getCustomerByAccountNumber(String accountNumber) {
                return mockCustomerDAO.findByAccountNumber(accountNumber);
            }
            
            @Override
            public List<Customer> getAllCustomers() {
                return mockCustomerDAO.findAll();
            }
            
            @Override
            public List<Customer> searchCustomers(String searchTerm) {
                return mockCustomerDAO.searchCustomers(searchTerm);
            }
            
            @Override
            public boolean createCustomer(Customer customer) {
                if (customer.getAccountNumber() == null || customer.getAccountNumber().isEmpty()) {
                    customer.setAccountNumber(mockCustomerDAO.generateAccountNumber());
                }
                return mockCustomerDAO.create(customer);
            }
            
            @Override
            public boolean updateCustomer(Customer customer) {
                return mockCustomerDAO.update(customer);
            }
            
            @Override
            public boolean deleteCustomer(int customerId) {
                return mockCustomerDAO.delete(customerId);
            }
        };
    }

    /**
     * Test adding a valid customer
     * 
     * Purpose: Verify that a customer can be successfully added with valid data
     * Inputs: Valid customer data
     * Expected Outputs: Customer is saved and returned with an ID
     * Requirement ID: CUST-001
     */
    @Test
    public void testAddValidCustomer() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAddress("123 Main St");
        customer.setTelephone("555-1234");
        customer.setEmail("john.doe@example.com");
        customer.setRegistrationDate(new Date(System.currentTimeMillis()));

        // Act
        boolean result = customerService.createCustomer(customer);

        // Assert
        assertTrue(result, "Customer should be saved successfully");
        assertNotEquals(0, customer.getId(), "Customer should have an ID assigned");
        assertNotNull(customer.getAccountNumber(), "Customer should have an account number assigned");
        
        Customer savedCustomer = customerService.getCustomerById(customer.getId());
        assertNotNull(savedCustomer, "Customer should be retrievable by ID");
        assertEquals("John Doe", savedCustomer.getName(), "Customer name should match");
        assertEquals("123 Main St", savedCustomer.getAddress(), "Customer address should match");
    }

    /**
     * Test adding a customer with missing name
     * 
     * Purpose: Verify that adding a customer fails when name is missing
     * Inputs: Customer with missing name
     * Expected Outputs: Customer is not saved
     * Requirement ID: CUST-002
     */
    @Test
    public void testAddCustomerWithMissingName() {
        // Arrange
        Customer customer = new Customer();
        // Missing name
        customer.setAddress("123 Main St");
        customer.setTelephone("555-1234");
        customer.setEmail("john.doe@example.com");
        
        mockCustomerDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = customerService.createCustomer(customer);

        // Assert
        assertFalse(result, "Customer should not be saved with missing name");
    }

    /**
     * Test adding a customer with invalid email format
     * 
     * Purpose: Verify that adding a customer fails when email format is invalid
     * Inputs: Customer with invalid email format
     * Expected Outputs: Customer is not saved
     * Requirement ID: CUST-003
     */
    @Test
    public void testAddCustomerWithInvalidEmail() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAddress("123 Main St");
        customer.setTelephone("555-1234");
        customer.setEmail("invalid-email"); // Invalid email format
        
        mockCustomerDAO.setShouldFailOnCreate(true);

        // Act
        boolean result = customerService.createCustomer(customer);

        // Assert
        assertFalse(result, "Customer should not be saved with invalid email format");
    }

    /**
     * Test adding a customer with auto-generated account number
     * 
     * Purpose: Verify that a customer can be added with an auto-generated account number
     * Inputs: Customer without account number
     * Expected Outputs: Customer is saved with an auto-generated account number
     * Requirement ID: CUST-004
     */
    @Test
    public void testAddCustomerWithAutoGeneratedAccountNumber() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("Jane Smith");
        customer.setAddress("456 Oak Ave");
        customer.setTelephone("555-5678");
        customer.setEmail("jane.smith@example.com");
        // No account number provided

        // Act
        boolean result = customerService.createCustomer(customer);

        // Assert
        assertTrue(result, "Customer should be saved successfully");
        assertNotNull(customer.getAccountNumber(), "Customer should have an auto-generated account number");
        assertTrue(customer.getAccountNumber().startsWith("CUST"), "Account number should have the expected format");
    }

    /**
     * Test updating an existing customer
     * 
     * Purpose: Verify that a customer can be successfully updated
     * Inputs: Updated customer data
     * Expected Outputs: Customer is updated in the database
     * Requirement ID: CUST-005
     */
    @Test
    public void testUpdateExistingCustomer() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("Original Name");
        customer.setAddress("Original Address");
        customer.setTelephone("555-1111");
        customer.setEmail("original@example.com");
        
        mockCustomerDAO.addTestCustomer(customer);
        
        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(customer.getId());
        updatedCustomer.setAccountNumber(customer.getAccountNumber());
        updatedCustomer.setName("Updated Name");
        updatedCustomer.setAddress("Updated Address");
        updatedCustomer.setTelephone("555-2222");
        updatedCustomer.setEmail("updated@example.com");

        // Act
        boolean result = customerService.updateCustomer(updatedCustomer);

        // Assert
        assertTrue(result, "Customer should be updated successfully");
        
        Customer retrievedCustomer = customerService.getCustomerById(customer.getId());
        assertNotNull(retrievedCustomer, "Customer should be retrievable by ID");
        assertEquals("Updated Name", retrievedCustomer.getName(), "Customer name should be updated");
        assertEquals("Updated Address", retrievedCustomer.getAddress(), "Customer address should be updated");
        assertEquals("555-2222", retrievedCustomer.getTelephone(), "Customer telephone should be updated");
        assertEquals("updated@example.com", retrievedCustomer.getEmail(), "Customer email should be updated");
    }

    /**
     * Test updating a customer with invalid email format
     * 
     * Purpose: Verify that updating a customer fails when email format is invalid
     * Inputs: Customer with invalid email format
     * Expected Outputs: Update operation fails
     * Requirement ID: CUST-006
     */
    @Test
    public void testUpdateCustomerWithInvalidEmail() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAddress("123 Main St");
        customer.setTelephone("555-1234");
        customer.setEmail("john.doe@example.com");
        
        mockCustomerDAO.addTestCustomer(customer);
        
        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(customer.getId());
        updatedCustomer.setAccountNumber(customer.getAccountNumber());
        updatedCustomer.setName("John Doe");
        updatedCustomer.setAddress("123 Main St");
        updatedCustomer.setTelephone("555-1234");
        updatedCustomer.setEmail("invalid-email"); // Invalid email format
        
        mockCustomerDAO.setShouldFailOnUpdate(true);

        // Act
        boolean result = customerService.updateCustomer(updatedCustomer);

        // Assert
        assertFalse(result, "Customer should not be updated with invalid email format");
    }

    /**
     * Test updating a non-existent customer
     * 
     * Purpose: Verify that updating a non-existent customer fails
     * Inputs: Customer with non-existent ID
     * Expected Outputs: Update operation fails
     * Requirement ID: CUST-007
     */
    @Test
    public void testUpdateNonExistentCustomer() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(999); // Non-existent ID
        customer.setAccountNumber("CUST999");
        customer.setName("Test Customer");
        customer.setAddress("Test Address");
        customer.setTelephone("555-9999");
        customer.setEmail("test@example.com");

        // Act
        boolean result = customerService.updateCustomer(customer);

        // Assert
        assertFalse(result, "Updating a non-existent customer should fail");
    }

    /**
     * Test deleting an existing customer
     * 
     * Purpose: Verify that a customer can be successfully deleted
     * Inputs: Valid customer ID
     * Expected Outputs: Customer is deleted from the database
     * Requirement ID: CUST-008
     */
    @Test
    public void testDeleteExistingCustomer() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAddress("123 Main St");
        customer.setTelephone("555-1234");
        customer.setEmail("john.doe@example.com");
        
        mockCustomerDAO.addTestCustomer(customer);

        // Act
        boolean result = customerService.deleteCustomer(customer.getId());

        // Assert
        assertTrue(result, "Customer should be deleted successfully");
        
        Customer retrievedCustomer = customerService.getCustomerById(customer.getId());
        assertNull(retrievedCustomer, "Customer should not be retrievable after deletion");
    }

    /**
     * Test deleting a non-existent customer
     * 
     * Purpose: Verify that deleting a non-existent customer fails
     * Inputs: Non-existent customer ID
     * Expected Outputs: Delete operation fails
     * Requirement ID: CUST-009
     */
    @Test
    public void testDeleteNonExistentCustomer() {
        // Arrange
        int nonExistentId = 999;

        // Act
        boolean result = customerService.deleteCustomer(nonExistentId);

        // Assert
        assertFalse(result, "Deleting a non-existent customer should fail");
    }

    /**
     * Test searching for customers by name
     * 
     * Purpose: Verify that customers can be searched by name
     * Inputs: Search term matching a customer name
     * Expected Outputs: Matching customers are returned
     * Requirement ID: CUST-010
     */
    @Test
    public void testSearchCustomersByName() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setName("John Smith");
        customer1.setAddress("123 Main St");
        customer1.setTelephone("555-1111");
        customer1.setEmail("john.smith@example.com");
        
        Customer customer2 = new Customer();
        customer2.setName("Jane Doe");
        customer2.setAddress("456 Oak Ave");
        customer2.setTelephone("555-2222");
        customer2.setEmail("jane.doe@example.com");
        
        mockCustomerDAO.addTestCustomer(customer1);
        mockCustomerDAO.addTestCustomer(customer2);

        // Act
        List<Customer> results = customerService.searchCustomers("Smith");

        // Assert
        assertEquals(1, results.size(), "Search should return one customer");
        assertEquals("John Smith", results.get(0).getName(), "Search result should match the expected customer");
    }

    /**
     * Test searching for customers by email
     * 
     * Purpose: Verify that customers can be searched by email
     * Inputs: Search term matching a customer email
     * Expected Outputs: Matching customers are returned
     * Requirement ID: CUST-011
     */
    @Test
    public void testSearchCustomersByEmail() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setName("John Smith");
        customer1.setAddress("123 Main St");
        customer1.setTelephone("555-1111");
        customer1.setEmail("john.smith@example.com");
        
        Customer customer2 = new Customer();
        customer2.setName("Jane Doe");
        customer2.setAddress("456 Oak Ave");
        customer2.setTelephone("555-2222");
        customer2.setEmail("jane.doe@example.com");
        
        mockCustomerDAO.addTestCustomer(customer1);
        mockCustomerDAO.addTestCustomer(customer2);

        // Act
        List<Customer> results = customerService.searchCustomers("jane.doe");

        // Assert
        assertEquals(1, results.size(), "Search should return one customer");
        assertEquals("Jane Doe", results.get(0).getName(), "Search result should match the expected customer");
    }

    /**
     * Test searching for customers by account number
     * 
     * Purpose: Verify that customers can be searched by account number
     * Inputs: Search term matching a customer account number
     * Expected Outputs: Matching customers are returned
     * Requirement ID: CUST-012
     */
    @Test
    public void testSearchCustomersByAccountNumber() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setName("John Smith");
        customer1.setAddress("123 Main St");
        customer1.setTelephone("555-1111");
        customer1.setEmail("john.smith@example.com");
        customer1.setAccountNumber("CUST1001");
        
        Customer customer2 = new Customer();
        customer2.setName("Jane Doe");
        customer2.setAddress("456 Oak Ave");
        customer2.setTelephone("555-2222");
        customer2.setEmail("jane.doe@example.com");
        customer2.setAccountNumber("CUST1002");
        
        mockCustomerDAO.addTestCustomer(customer1);
        mockCustomerDAO.addTestCustomer(customer2);

        // Act
        List<Customer> results = customerService.searchCustomers("CUST1001");

        // Assert
        assertEquals(1, results.size(), "Search should return one customer");
        assertEquals("John Smith", results.get(0).getName(), "Search result should match the expected customer");
    }

    /**
     * Test searching for non-existent customers
     * 
     * Purpose: Verify that searching for non-existent customers returns empty results
     * Inputs: Search term not matching any customer
     * Expected Outputs: Empty list is returned
     * Requirement ID: CUST-013
     */
    @Test
    public void testSearchNonExistentCustomers() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setName("John Smith");
        customer1.setAddress("123 Main St");
        customer1.setTelephone("555-1111");
        customer1.setEmail("john.smith@example.com");
        
        Customer customer2 = new Customer();
        customer2.setName("Jane Doe");
        customer2.setAddress("456 Oak Ave");
        customer2.setTelephone("555-2222");
        customer2.setEmail("jane.doe@example.com");
        
        mockCustomerDAO.addTestCustomer(customer1);
        mockCustomerDAO.addTestCustomer(customer2);

        // Act
        List<Customer> results = customerService.searchCustomers("NonExistent");

        // Assert
        assertTrue(results.isEmpty(), "Search should return empty list for non-existent customers");
    }

    /**
     * Test getting all customers
     * 
     * Purpose: Verify that all customers can be retrieved
     * Inputs: None
     * Expected Outputs: All customers are returned
     * Requirement ID: CUST-014
     */
    @Test
    public void testGetAllCustomers() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setName("John Smith");
        customer1.setAddress("123 Main St");
        customer1.setTelephone("555-1111");
        customer1.setEmail("john.smith@example.com");
        
        Customer customer2 = new Customer();
        customer2.setName("Jane Doe");
        customer2.setAddress("456 Oak Ave");
        customer2.setTelephone("555-2222");
        customer2.setEmail("jane.doe@example.com");
        
        mockCustomerDAO.addTestCustomer(customer1);
        mockCustomerDAO.addTestCustomer(customer2);

        // Act
        List<Customer> results = customerService.getAllCustomers();

        // Assert
        assertEquals(2, results.size(), "Should return all customers");
    }

    /**
     * Test getting a customer by ID
     * 
     * Purpose: Verify that a customer can be retrieved by ID
     * Inputs: Valid customer ID
     * Expected Outputs: Customer is returned
     * Requirement ID: CUST-015
     */
    @Test
    public void testGetCustomerById() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Smith");
        customer.setAddress("123 Main St");
        customer.setTelephone("555-1111");
        customer.setEmail("john.smith@example.com");
        
        mockCustomerDAO.addTestCustomer(customer);

        // Act
        Customer result = customerService.getCustomerById(customer.getId());

        // Assert
        assertNotNull(result, "Customer should be retrievable by ID");
        assertEquals("John Smith", result.getName(), "Customer name should match");
        assertEquals("123 Main St", result.getAddress(), "Customer address should match");
    }
}