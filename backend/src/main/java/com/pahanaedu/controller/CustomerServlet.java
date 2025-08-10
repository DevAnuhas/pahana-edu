package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pahanaedu.model.Customer;
import com.pahanaedu.model.User;
import com.pahanaedu.service.AuthService;
import com.pahanaedu.service.CustomerService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for handling customer-related API requests
 */
@WebServlet(name = "CustomerServlet", urlPatterns = {"/api/customers", "/api/customers/*"})
public class CustomerServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CustomerServlet.class.getName());
    private final CustomerService customerService = new CustomerService();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    /**
     * Get customers - either all customers or a specific customer by ID
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is authenticated
        User user = authService.getUserFromSession(request);
        if (user == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific customer ID is requested
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all customers or search by query parameter
                String searchTerm = request.getParameter("search");
                List<Customer> customers;

                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    customers = customerService.searchCustomers(searchTerm);
                } else {
                    customers = customerService.getAllCustomers();
                }

                out.print(gson.toJson(customers));

            } else {
                // Get specific customer by ID
                String customerId = pathInfo.substring(1); // Remove leading slash

                try {
                    int id = Integer.parseInt(customerId);
                    Customer customer = customerService.getCustomerById(id);

                    if (customer != null) {
                        out.print(gson.toJson(customer));
                    } else {
                        sendNotFoundResponse(response, "Customer not found with ID: " + id);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Check if it's an account number request
                    if (customerId.startsWith("account/")) {
                        String accountNumber = customerId.substring("account/".length());
                        Customer customer = customerService.getCustomerByAccountNumber(accountNumber);

                        if (customer != null) {
                            out.print(gson.toJson(customer));
                        } else {
                            sendNotFoundResponse(response, "Customer not found with account number: " + accountNumber);
                            return;
                        }
                    } else {
                        sendBadRequestResponse(response, "Invalid customer ID format");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing customer request", e);
            sendErrorResponse(response, "Error processing request: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Create a new customer
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is authenticated
        User user = authService.getUserFromSession(request);
        if (user == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Read request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            // Parse JSON request
            JsonObject jsonRequest = new JsonParser().parse(requestBody.toString()).getAsJsonObject();

            // Create customer object from JSON
            Customer customer = new Customer();

            // Required fields
            if (jsonRequest.has("name") && !jsonRequest.get("name").getAsString().trim().isEmpty()) {
                customer.setName(jsonRequest.get("name").getAsString());
            } else {
                sendBadRequestResponse(response, "Customer name is required");
                return;
            }

            if (jsonRequest.has("address") && !jsonRequest.get("address").getAsString().trim().isEmpty()) {
                customer.setAddress(jsonRequest.get("address").getAsString());
            } else {
                sendBadRequestResponse(response, "Customer address is required");
                return;
            }

            if (jsonRequest.has("telephone") && !jsonRequest.get("telephone").getAsString().trim().isEmpty()) {
                customer.setTelephone(jsonRequest.get("telephone").getAsString());
            } else {
                sendBadRequestResponse(response, "Customer telephone is required");
                return;
            }

            // Optional fields
            if (jsonRequest.has("accountNumber") && !jsonRequest.get("accountNumber").getAsString().trim().isEmpty()) {
                customer.setAccountNumber(jsonRequest.get("accountNumber").getAsString());
            }

            if (jsonRequest.has("email")) {
                customer.setEmail(jsonRequest.get("email").getAsString());
            }

            // Set registration date to current date if not provided
            if (jsonRequest.has("registrationDate")) {
                customer.setRegistrationDate(Date.valueOf(jsonRequest.get("registrationDate").getAsString()));
            } else {
                customer.setRegistrationDate(new Date(System.currentTimeMillis()));
            }

            // Create customer
            boolean success = customerService.createCustomer(customer);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Customer created successfully");
                jsonResponse.add("customer", gson.toJsonTree(customer));

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to create customer");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating customer", e);
            sendErrorResponse(response, "Error creating customer: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Update an existing customer
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is authenticated
        User user = authService.getUserFromSession(request);
        if (user == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific customer ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Customer ID is required for update");
                return;
            }

            // Get customer ID from path
            String customerId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(customerId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid customer ID format");
                return;
            }

            // Check if customer exists
            Customer existingCustomer = customerService.getCustomerById(id);

            if (existingCustomer == null) {
                sendNotFoundResponse(response, "Customer not found with ID: " + id);
                return;
            }

            // Read request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            // Parse JSON request
            JsonObject jsonRequest = new JsonParser().parse(requestBody.toString()).getAsJsonObject();

            // Update customer object from JSON
            if (jsonRequest.has("name") && !jsonRequest.get("name").getAsString().trim().isEmpty()) {
                existingCustomer.setName(jsonRequest.get("name").getAsString());
            }

            if (jsonRequest.has("address") && !jsonRequest.get("address").getAsString().trim().isEmpty()) {
                existingCustomer.setAddress(jsonRequest.get("address").getAsString());
            }

            if (jsonRequest.has("telephone") && !jsonRequest.get("telephone").getAsString().trim().isEmpty()) {
                existingCustomer.setTelephone(jsonRequest.get("telephone").getAsString());
            }

            if (jsonRequest.has("email")) {
                existingCustomer.setEmail(jsonRequest.get("email").getAsString());
            }

            // Update customer
            boolean success = customerService.updateCustomer(existingCustomer);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Customer updated successfully");
                jsonResponse.add("customer", gson.toJsonTree(existingCustomer));

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to update customer");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating customer", e);
            sendErrorResponse(response, "Error updating customer: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Delete a customer
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is authenticated
        User user = authService.getUserFromSession(request);
        if (user == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        // Check if user has admin role
        if (!"ADMIN".equals(user.getRole())) {
            sendForbiddenResponse(response, "Only administrators can delete customers");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific customer ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Customer ID is required for deletion");
                return;
            }

            // Get customer ID from path
            String customerId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(customerId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid customer ID format");
                return;
            }

            // Check if customer exists
            Customer existingCustomer = customerService.getCustomerById(id);

            if (existingCustomer == null) {
                sendNotFoundResponse(response, "Customer not found with ID: " + id);
                return;
            }

            // Delete customer
            boolean success = customerService.deleteCustomer(id);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Customer deleted successfully");

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to delete customer. The customer may have associated invoices.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting customer", e);
            sendErrorResponse(response, "Error deleting customer: " + e.getMessage());
            return;
        }

        out.flush();
    }

    // Helper methods for sending standardized responses

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "error");
        jsonResponse.addProperty("message", "Authentication required");

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "error");
        jsonResponse.addProperty("message", message);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    private void sendBadRequestResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "error");
        jsonResponse.addProperty("message", message);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    private void sendNotFoundResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "error");
        jsonResponse.addProperty("message", message);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "error");
        jsonResponse.addProperty("message", message);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}
