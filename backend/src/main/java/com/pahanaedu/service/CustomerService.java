package com.pahanaedu.service;

import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.model.Customer;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for handling customer-related business logic
 */
public class CustomerService {
    private static final Logger LOGGER = Logger.getLogger(CustomerService.class.getName());
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public Customer getCustomerById(int id) {
        LOGGER.info("Fetching customer with ID: " + id);
        return customerDAO.findById(id);
    }

    public Customer getCustomerByAccountNumber(String accountNumber) {
        LOGGER.info("Fetching customer with account number: " + accountNumber);
        return customerDAO.findByAccountNumber(accountNumber);
    }

    public List<Customer> getAllCustomers() {
        LOGGER.info("Fetching all customers");
        return customerDAO.findAll();
    }

    public List<Customer> searchCustomers(String searchTerm) {
        LOGGER.info("Searching customers with term: " + searchTerm);
        return customerDAO.searchCustomers(searchTerm);
    }

    public boolean createCustomer(Customer customer) {
        LOGGER.info("Creating new customer: " + customer.getName());

        // Generate account number if not provided
        if (customer.getAccountNumber() == null || customer.getAccountNumber().isEmpty()) {
            customer.setAccountNumber(customerDAO.generateAccountNumber());
        }

        return customerDAO.create(customer);
    }

    public boolean updateCustomer(Customer customer) {
        LOGGER.info("Updating customer with ID: " + customer.getId());
        return customerDAO.update(customer);
    }

    public boolean deleteCustomer(int customerId) {
        LOGGER.info("Deleting customer with ID: " + customerId);
        return customerDAO.delete(customerId);
    }
}
