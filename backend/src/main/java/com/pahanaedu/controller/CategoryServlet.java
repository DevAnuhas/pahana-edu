package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pahanaedu.model.Category;
import com.pahanaedu.model.User;
import com.pahanaedu.service.AuthService;
import com.pahanaedu.service.BookService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for handling category-related API requests
 */
@WebServlet(name = "CategoryServlet", urlPatterns = {"/api/categories", "/api/categories/*"})
public class CategoryServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CategoryServlet.class.getName());
    private final BookService bookService = new BookService();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    /**
     * Get categories - either all categories or a specific category by ID
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
            // Check if a specific category ID is requested
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all categories
                List<Category> categories = bookService.getAllCategories();
                out.print(gson.toJson(categories));

            } else {
                // Get specific category by ID
                String categoryId = pathInfo.substring(1); // Remove leading slash

                try {
                    int id = Integer.parseInt(categoryId);
                    Category category = bookService.getCategoryById(id);

                    if (category != null) {
                        out.print(gson.toJson(category));
                    } else {
                        sendNotFoundResponse(response, "Category not found with ID: " + id);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendBadRequestResponse(response, "Invalid category ID format");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing category request", e);
            sendErrorResponse(response, "Error processing request: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Create a new category
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

            // Create category object from JSON
            Category category = new Category();

            // Required fields
            if (jsonRequest.has("name") && !jsonRequest.get("name").getAsString().trim().isEmpty()) {
                category.setName(jsonRequest.get("name").getAsString());
            } else {
                sendBadRequestResponse(response, "Category name is required");
                return;
            }

            // Optional fields
            if (jsonRequest.has("description")) {
                category.setDescription(jsonRequest.get("description").getAsString());
            }

            // Create category
            boolean success = bookService.createCategory(category);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Category created successfully");
                jsonResponse.add("category", gson.toJsonTree(category));

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to create category. The name may already be in use.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating category", e);
            sendErrorResponse(response, "Error creating category: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Update an existing category
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
            // Check if a specific category ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Category ID is required for update");
                return;
            }

            // Get category ID from path
            String categoryId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(categoryId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid category ID format");
                return;
            }

            // Check if category exists
            Category existingCategory = bookService.getCategoryById(id);

            if (existingCategory == null) {
                sendNotFoundResponse(response, "Category not found with ID: " + id);
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

            // Update category object from JSON
            if (jsonRequest.has("name") && !jsonRequest.get("name").getAsString().trim().isEmpty()) {
                existingCategory.setName(jsonRequest.get("name").getAsString());
            }

            if (jsonRequest.has("description")) {
                existingCategory.setDescription(jsonRequest.get("description").getAsString());
            }

            // Update category
            boolean success = bookService.updateCategory(existingCategory);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Category updated successfully");
                jsonResponse.add("category", gson.toJsonTree(existingCategory));

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to update category. The name may already be in use.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating category", e);
            sendErrorResponse(response, "Error updating category: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Delete a category
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
            sendForbiddenResponse(response, "Only administrators can delete categories");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific category ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Category ID is required for deletion");
                return;
            }

            // Get category ID from path
            String categoryId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(categoryId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid category ID format");
                return;
            }

            // Check if category exists
            Category existingCategory = bookService.getCategoryById(id);

            if (existingCategory == null) {
                sendNotFoundResponse(response, "Category not found with ID: " + id);
                return;
            }

            // Delete category
            boolean success = bookService.deleteCategory(id);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Category deleted successfully");

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to delete category. The category may be in use by books.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting category", e);
            sendErrorResponse(response, "Error deleting category: " + e.getMessage());
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
