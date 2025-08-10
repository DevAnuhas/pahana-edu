package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pahanaedu.model.Publisher;
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
 * Controller for handling publisher-related API requests
 */
@WebServlet(name = "PublisherServlet", urlPatterns = {"/api/publishers", "/api/publishers/*"})
public class PublisherServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PublisherServlet.class.getName());
    private final BookService bookService = new BookService();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    /**
     * Get publishers - either all publishers or a specific publisher by ID
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
            // Check if a specific publisher ID is requested
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all publishers
                List<Publisher> publishers = bookService.getAllPublishers();
                out.print(gson.toJson(publishers));

            } else {
                // Get specific publisher by ID
                String publisherId = pathInfo.substring(1); // Remove leading slash

                try {
                    int id = Integer.parseInt(publisherId);
                    Publisher publisher = bookService.getPublisherById(id);

                    if (publisher != null) {
                        out.print(gson.toJson(publisher));
                    } else {
                        sendNotFoundResponse(response, "Publisher not found with ID: " + id);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendBadRequestResponse(response, "Invalid publisher ID format");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing publisher request", e);
            sendErrorResponse(response, "Error processing request: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Create a new publisher
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

            // Create publisher object from JSON
            Publisher publisher = new Publisher();

            // Required fields
            if (jsonRequest.has("name") && !jsonRequest.get("name").getAsString().trim().isEmpty()) {
                publisher.setName(jsonRequest.get("name").getAsString());
            } else {
                sendBadRequestResponse(response, "Publisher name is required");
                return;
            }

            // Optional fields
            if (jsonRequest.has("contactPerson")) {
                publisher.setContactPerson(jsonRequest.get("contactPerson").getAsString());
            }

            if (jsonRequest.has("telephone")) {
                publisher.setTelephone(jsonRequest.get("telephone").getAsString());
            }

            if (jsonRequest.has("email")) {
                publisher.setEmail(jsonRequest.get("email").getAsString());
            }

            if (jsonRequest.has("address")) {
                publisher.setAddress(jsonRequest.get("address").getAsString());
            }

            // Create publisher
            boolean success = bookService.createPublisher(publisher);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Publisher created successfully");
                jsonResponse.add("publisher", gson.toJsonTree(publisher));

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to create publisher. The name may already be in use.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating publisher", e);
            sendErrorResponse(response, "Error creating publisher: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Update an existing publisher
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
            // Check if a specific publisher ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Publisher ID is required for update");
                return;
            }

            // Get publisher ID from path
            String publisherId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(publisherId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid publisher ID format");
                return;
            }

            // Check if publisher exists
            Publisher existingPublisher = bookService.getPublisherById(id);

            if (existingPublisher == null) {
                sendNotFoundResponse(response, "Publisher not found with ID: " + id);
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

            // Update publisher object from JSON
            if (jsonRequest.has("name") && !jsonRequest.get("name").getAsString().trim().isEmpty()) {
                existingPublisher.setName(jsonRequest.get("name").getAsString());
            }

            if (jsonRequest.has("contactPerson")) {
                existingPublisher.setContactPerson(jsonRequest.get("contactPerson").getAsString());
            }

            if (jsonRequest.has("telephone")) {
                existingPublisher.setTelephone(jsonRequest.get("telephone").getAsString());
            }

            if (jsonRequest.has("email")) {
                existingPublisher.setEmail(jsonRequest.get("email").getAsString());
            }

            if (jsonRequest.has("address")) {
                existingPublisher.setAddress(jsonRequest.get("address").getAsString());
            }

            // Update publisher
            boolean success = bookService.updatePublisher(existingPublisher);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Publisher updated successfully");
                jsonResponse.add("publisher", gson.toJsonTree(existingPublisher));

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to update publisher. The name may already be in use.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating publisher", e);
            sendErrorResponse(response, "Error updating publisher: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Delete a publisher
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
            sendForbiddenResponse(response, "Only administrators can delete publishers");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific publisher ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Publisher ID is required for deletion");
                return;
            }

            // Get publisher ID from path
            String publisherId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(publisherId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid publisher ID format");
                return;
            }

            // Check if publisher exists
            Publisher existingPublisher = bookService.getPublisherById(id);

            if (existingPublisher == null) {
                sendNotFoundResponse(response, "Publisher not found with ID: " + id);
                return;
            }

            // Delete publisher
            boolean success = bookService.deletePublisher(id);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Publisher deleted successfully");

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to delete publisher. The publisher may be in use by books.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting publisher", e);
            sendErrorResponse(response, "Error deleting publisher: " + e.getMessage());
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
