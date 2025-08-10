package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pahanaedu.model.Book;
import com.pahanaedu.model.Category;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for handling book-related API requests
 */
@WebServlet(name = "BookServlet", urlPatterns = {"/api/books", "/api/books/*"})
public class BookServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(BookServlet.class.getName());
    private final BookService bookService = new BookService();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = authService.getUserFromSession(request);
        if (user == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                String searchTerm = request.getParameter("search");
                String categoryParam = request.getParameter("category");
                List<Book> books;

                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    books = bookService.searchBooks(searchTerm);
                } else if (categoryParam != null && !categoryParam.trim().isEmpty()) {
                    try {
                        int categoryId = Integer.parseInt(categoryParam);
                        books = bookService.getBooksByCategory(categoryId);
                    } catch (NumberFormatException e) {
                        sendBadRequestResponse(response, "Invalid category ID format");
                        return;
                    }
                } else {
                    books = bookService.getAllBooks();
                }

                out.print(gson.toJson(books));

            } else {
                // Get specific book by ID or ISBN
                String bookId = pathInfo.substring(1);

                try {
                    if (bookId.startsWith("isbn/")) {
                        // Lookup by ISBN
                        String isbn = bookId.substring("isbn/".length());
                        Book book = bookService.getBookByIsbn(isbn);

                        if (book != null) {
                            out.print(gson.toJson(book));
                        } else {
                            sendNotFoundResponse(response, "Book not found with ISBN: " + isbn);
                            return;
                        }
                    } else {
                        // Lookup by ID
                        int id = Integer.parseInt(bookId);
                        Book book = bookService.getBookById(id);

                        if (book != null) {
                            out.print(gson.toJson(book));
                        } else {
                            sendNotFoundResponse(response, "Book not found with ID: " + id);
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    sendBadRequestResponse(response, "Invalid book ID format");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing book request", e);
            sendErrorResponse(response, "Error processing request: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Create a new book
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = authService.getUserFromSession(request);
        if (user == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            JsonObject jsonRequest = new JsonParser().parse(requestBody.toString()).getAsJsonObject();

            Book book = new Book();

            if (jsonRequest.has("isbn") && !jsonRequest.get("isbn").getAsString().trim().isEmpty()) {
                book.setIsbn(jsonRequest.get("isbn").getAsString());
            } else {
                sendBadRequestResponse(response, "Book ISBN is required");
                return;
            }

            if (jsonRequest.has("title") && !jsonRequest.get("title").getAsString().trim().isEmpty()) {
                book.setTitle(jsonRequest.get("title").getAsString());
            } else {
                sendBadRequestResponse(response, "Book title is required");
                return;
            }

            if (jsonRequest.has("author") && !jsonRequest.get("author").getAsString().trim().isEmpty()) {
                book.setAuthor(jsonRequest.get("author").getAsString());
            } else {
                sendBadRequestResponse(response, "Book author is required");
                return;
            }

            if (jsonRequest.has("categoryId")) {
                book.setCategoryId(jsonRequest.get("categoryId").getAsInt());
            } else {
                sendBadRequestResponse(response, "Category ID is required");
                return;
            }

            if (jsonRequest.has("publisherId")) {
                book.setPublisherId(jsonRequest.get("publisherId").getAsInt());
            } else {
                sendBadRequestResponse(response, "Publisher ID is required");
                return;
            }

            if (jsonRequest.has("price")) {
                book.setPrice(new BigDecimal(jsonRequest.get("price").getAsString()));
            } else {
                sendBadRequestResponse(response, "Book price is required");
                return;
            }

            // Optional fields
            if (jsonRequest.has("publicationYear")) {
                book.setPublicationYear(jsonRequest.get("publicationYear").getAsInt());
            }

            if (jsonRequest.has("stockQuantity")) {
                book.setStockQuantity(jsonRequest.get("stockQuantity").getAsInt());
            } else {
                book.setStockQuantity(0); // Default to 0
            }

            if (jsonRequest.has("description")) {
                book.setDescription(jsonRequest.get("description").getAsString());
            }

            // Check if book with ISBN already exists
            Book existingBook = bookService.getBookByIsbn(book.getIsbn());
            if (existingBook != null) {
                sendBadRequestResponse(response, "A book with ISBN " + book.getIsbn() + " already exists");
                return;
            }

            // Create book
            boolean success = bookService.createBook(book);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Book created successfully");
                jsonResponse.add("book", gson.toJsonTree(book));

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to create book. Please check category and publisher IDs.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating book", e);
            sendErrorResponse(response, "Error creating book: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Update an existing book
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
            // Check if a specific book ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Book ID is required for update");
                return;
            }

            // Get book ID from path
            String bookId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(bookId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid book ID format");
                return;
            }

            // Check if book exists
            Book existingBook = bookService.getBookById(id);

            if (existingBook == null) {
                sendNotFoundResponse(response, "Book not found with ID: " + id);
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

            // Update book object from JSON
            if (jsonRequest.has("title") && !jsonRequest.get("title").getAsString().trim().isEmpty()) {
                existingBook.setTitle(jsonRequest.get("title").getAsString());
            }

            if (jsonRequest.has("author") && !jsonRequest.get("author").getAsString().trim().isEmpty()) {
                existingBook.setAuthor(jsonRequest.get("author").getAsString());
            }

            if (jsonRequest.has("categoryId")) {
                existingBook.setCategoryId(jsonRequest.get("categoryId").getAsInt());
            }

            if (jsonRequest.has("publisherId")) {
                existingBook.setPublisherId(jsonRequest.get("publisherId").getAsInt());
            }

            if (jsonRequest.has("publicationYear")) {
                existingBook.setPublicationYear(jsonRequest.get("publicationYear").getAsInt());
            }

            if (jsonRequest.has("price")) {
                existingBook.setPrice(new BigDecimal(jsonRequest.get("price").getAsString()));
            }

            if (jsonRequest.has("stockQuantity")) {
                existingBook.setStockQuantity(jsonRequest.get("stockQuantity").getAsInt());
            }

            if (jsonRequest.has("description")) {
                existingBook.setDescription(jsonRequest.get("description").getAsString());
            }

            // Update book
            boolean success = bookService.updateBook(existingBook);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Book updated successfully");
                jsonResponse.add("book", gson.toJsonTree(existingBook));

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to update book. Please check category and publisher IDs.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating book", e);
            sendErrorResponse(response, "Error updating book: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Delete a book
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
            sendForbiddenResponse(response, "Only administrators can delete books");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific book ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Book ID is required for deletion");
                return;
            }

            // Get book ID from path
            String bookId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(bookId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid book ID format");
                return;
            }

            // Check if book exists
            Book existingBook = bookService.getBookById(id);

            if (existingBook == null) {
                sendNotFoundResponse(response, "Book not found with ID: " + id);
                return;
            }

            // Delete book
            boolean success = bookService.deleteBook(id);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Book deleted successfully");

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to delete book. The book may be referenced in invoices.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting book", e);
            sendErrorResponse(response, "Error deleting book: " + e.getMessage());
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
