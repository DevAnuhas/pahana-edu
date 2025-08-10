package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pahanaedu.model.Invoice;
import com.pahanaedu.model.InvoiceItem;
import com.pahanaedu.model.User;
import com.pahanaedu.service.AuthService;
import com.pahanaedu.service.BillingService;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for handling invoice and billing-related API requests
 */
@WebServlet(name = "InvoiceServlet", urlPatterns = {"/api/invoices", "/api/invoices/*"})
public class InvoiceServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(InvoiceServlet.class.getName());
    private final BillingService billingService = new BillingService();
    private final BookService bookService = new BookService();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    /**
     * Get invoices - either all invoices, invoices for a customer, or a specific invoice by ID
     */
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
                // Get all invoices or filter by customer
                String customerParam = request.getParameter("customer");
                List<Invoice> invoices;

                if (customerParam != null && !customerParam.trim().isEmpty()) {
                    try {
                        int customerId = Integer.parseInt(customerParam);
                        invoices = billingService.getInvoicesByCustomer(customerId);
                    } catch (NumberFormatException e) {
                        sendBadRequestResponse(response, "Invalid customer ID format");
                        return;
                    }
                } else {
                    invoices = billingService.getAllInvoices();
                }

                out.print(gson.toJson(invoices));

            } else if (pathInfo.startsWith("/print/")) {
                // Print invoice
                String invoiceId = pathInfo.substring("/print/".length());
                try {
                    int id = Integer.parseInt(invoiceId);
                    Invoice invoice = billingService.getInvoiceById(id);

                    if (invoice != null) {
                        String printableInvoice = billingService.generatePrintableBill(invoice);

                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.addProperty("invoiceId", id);
                        jsonResponse.addProperty("printableInvoice", printableInvoice);

                        out.print(gson.toJson(jsonResponse));
                    } else {
                        sendNotFoundResponse(response, "Invoice not found with ID: " + id);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendBadRequestResponse(response, "Invalid invoice ID format");
                    return;
                }
            } else {
                // Get specific invoice by ID or number
                String invoiceId = pathInfo.substring(1);

                try {
                    if (invoiceId.startsWith("number/")) {
                        // Lookup by invoice number
                        String invoiceNumber = invoiceId.substring("number/".length());
                        Invoice invoice = billingService.getInvoiceByNumber(invoiceNumber);

                        if (invoice != null) {
                            out.print(gson.toJson(invoice));
                        } else {
                            sendNotFoundResponse(response, "Invoice not found with number: " + invoiceNumber);
                            return;
                        }
                    } else {
                        // Lookup by ID
                        int id = Integer.parseInt(invoiceId);
                        Invoice invoice = billingService.getInvoiceById(id);

                        if (invoice != null) {
                            out.print(gson.toJson(invoice));
                        } else {
                            sendNotFoundResponse(response, "Invoice not found with ID: " + id);
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    sendBadRequestResponse(response, "Invalid invoice ID format");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing invoice request", e);
            sendErrorResponse(response, "Error processing request: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Create a new invoice
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
            // Check if this is a preview request
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.equals("/preview")) {
                handlePreviewRequest(request, response, user);
                return;
            }
            
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
   
            JsonObject jsonRequest = JsonParser.parseString(requestBody.toString()).getAsJsonObject();

            Invoice invoice = new Invoice();

            invoice.setCashierId(user.getId());
            invoice.setCashierName(user.getFullName());
            invoice.setInvoiceDate(new Timestamp(System.currentTimeMillis()));

            if (jsonRequest.has("invoiceNumber") && !jsonRequest.get("invoiceNumber").isJsonNull()) {
                invoice.setInvoiceNumber(jsonRequest.get("invoiceNumber").getAsString());
            }

            if (jsonRequest.has("customerId") && !jsonRequest.get("customerId").isJsonNull()) {
                invoice.setCustomerId(jsonRequest.get("customerId").getAsInt());
            }

            if (jsonRequest.has("customerName") && !jsonRequest.get("customerName").isJsonNull()) {
                invoice.setCustomerName(jsonRequest.get("customerName").getAsString());
            }

            if (jsonRequest.has("paymentMethod") && !jsonRequest.get("paymentMethod").getAsString().isEmpty()) {
                invoice.setPaymentMethod(jsonRequest.get("paymentMethod").getAsString());
            } else {
                invoice.setPaymentMethod("CASH");
            }

            if (jsonRequest.has("notes")) {
                invoice.setNotes(jsonRequest.get("notes").getAsString());
            }

            if (jsonRequest.has("items") && jsonRequest.get("items").isJsonArray()) {
                JsonArray itemsArray = jsonRequest.get("items").getAsJsonArray();
                List<InvoiceItem> items = new ArrayList<>();

                for (int i = 0; i < itemsArray.size(); i++) {
                    JsonObject itemJson = itemsArray.get(i).getAsJsonObject();
                    InvoiceItem item = new InvoiceItem();

                    // Required fields
                    if (itemJson.has("bookId")) {
                        item.setBookId(itemJson.get("bookId").getAsInt());
                    } else {
                        sendBadRequestResponse(response, "Book ID is required for invoice item");
                        return;
                    }

                    if (itemJson.has("quantity")) {
                        item.setQuantity(itemJson.get("quantity").getAsInt());
                    } else {
                        sendBadRequestResponse(response, "Quantity is required for invoice item");
                        return;
                    }

                    if (itemJson.has("unitPrice")) {
                        if (itemJson.get("unitPrice").isJsonPrimitive()) {
                            item.setUnitPrice(new BigDecimal(itemJson.get("unitPrice").getAsString()));
                        }
                    }

                    if (itemJson.has("discountPercent")) {
                        if (itemJson.get("discountPercent").isJsonPrimitive()) {
                            item.setDiscountPercent(new BigDecimal(itemJson.get("discountPercent").getAsString()));
                        }
                    } else {
                        item.setDiscountPercent(BigDecimal.ZERO);
                    }

                    if (itemJson.has("bookTitle")) {
                        item.setBookTitle(itemJson.get("bookTitle").getAsString());
                    }

                    items.add(item);
                }

                invoice.setItems(items);
            } else {
                sendBadRequestResponse(response, "Invoice items are required");
                return;
            }

            // Handle discountAmount
            if (jsonRequest.has("discountAmount")) {
                if (jsonRequest.get("discountAmount").isJsonPrimitive()) {
                    invoice.setDiscountAmount(new BigDecimal(jsonRequest.get("discountAmount").getAsString()));
                }
            }

            boolean applyTax = false;
            if (jsonRequest.has("applyTax")) {
                applyTax = jsonRequest.get("applyTax").getAsBoolean();
            }

            invoice = billingService.calculateBill(invoice, applyTax);

            boolean success = billingService.createInvoice(invoice);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Invoice created successfully");
                jsonResponse.add("invoice", gson.toJsonTree(invoice));

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to create invoice. Please check item availability.");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating invoice", e);
            sendErrorResponse(response, "Error creating invoice: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Delete an invoice
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
            sendForbiddenResponse(response, "Only administrators can delete invoices");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if a specific invoice ID is provided
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequestResponse(response, "Invoice ID is required for deletion");
                return;
            }

            // Get invoice ID from path
            String invoiceId = pathInfo.substring(1); // Remove leading slash
            int id;

            try {
                id = Integer.parseInt(invoiceId);
            } catch (NumberFormatException e) {
                sendBadRequestResponse(response, "Invalid invoice ID format");
                return;
            }

            // Check if invoice exists
            Invoice existingInvoice = billingService.getInvoiceById(id);

            if (existingInvoice == null) {
                sendNotFoundResponse(response, "Invoice not found with ID: " + id);
                return;
            }

            // Delete invoice
            boolean success = billingService.deleteInvoice(id);

            if (success) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Invoice deleted successfully");

                out.print(gson.toJson(jsonResponse));
            } else {
                sendErrorResponse(response, "Failed to delete invoice");
                return;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting invoice", e);
            sendErrorResponse(response, "Error deleting invoice: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Calculate a bill without saving it
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
            // Check if action is calculate
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || !pathInfo.equals("/calculate")) {
                sendBadRequestResponse(response, "Invalid action. Use /calculate to calculate bill");
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
            JsonObject jsonRequest = JsonParser.parseString(requestBody.toString()).getAsJsonObject();

            // Create invoice object from JSON
            Invoice invoice = new Invoice();

            // Set cashier ID to current user
            invoice.setCashierId(user.getId());
            invoice.setCashierName(user.getFullName());

            // Set invoice date if provided
            if (jsonRequest.has("invoiceDate") && !jsonRequest.get("invoiceDate").isJsonNull()) {
                try {
                    // Try to parse in format yyyy-MM-dd HH:mm:ss
                    String dateString = jsonRequest.get("invoiceDate").getAsString();
                    
                    // Handle common date formats
                    if (dateString.contains("T")) {
                        // ISO format like 2023-12-31T12:30:45
                        dateString = dateString.replace("T", " ");
                        // Remove timezone part if exists
                        if (dateString.contains("+")) {
                            dateString = dateString.substring(0, dateString.indexOf("+"));
                        }
                        if (dateString.contains("Z")) {
                            dateString = dateString.substring(0, dateString.indexOf("Z"));
                        }
                    }
                    
                    try {
                        invoice.setInvoiceDate(Timestamp.valueOf(dateString));
                    } catch (IllegalArgumentException e) {
                        LOGGER.log(Level.WARNING, "Invalid date format: " + dateString + ". Using current timestamp.");
                        invoice.setInvoiceDate(new Timestamp(System.currentTimeMillis()));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error parsing invoice date", e);
                    invoice.setInvoiceDate(new Timestamp(System.currentTimeMillis()));
                }
            } else {
                invoice.setInvoiceDate(new Timestamp(System.currentTimeMillis()));
            }

            // Optional customer ID
            if (jsonRequest.has("customerId") && !jsonRequest.get("customerId").isJsonNull()) {
                invoice.setCustomerId(jsonRequest.get("customerId").getAsInt());
            }

            // Invoice items
            if (jsonRequest.has("items") && jsonRequest.get("items").isJsonArray()) {
                JsonArray itemsArray = jsonRequest.get("items").getAsJsonArray();
                List<InvoiceItem> items = new ArrayList<>();

                for (int i = 0; i < itemsArray.size(); i++) {
                    JsonObject itemJson = itemsArray.get(i).getAsJsonObject();
                    InvoiceItem item = new InvoiceItem();

                    // Required fields
                    if (itemJson.has("bookId")) {
                        item.setBookId(itemJson.get("bookId").getAsInt());
                    } else {
                        sendBadRequestResponse(response, "Book ID is required for invoice item");
                        return;
                    }

                    if (itemJson.has("quantity")) {
                        item.setQuantity(itemJson.get("quantity").getAsInt());
                    } else {
                        sendBadRequestResponse(response, "Quantity is required for invoice item");
                        return;
                    }

                    // Optional fields
                    if (itemJson.has("unitPrice")) {
                        item.setUnitPrice(new BigDecimal(itemJson.get("unitPrice").getAsString()));
                    }

                    if (itemJson.has("discountPercent")) {
                        item.setDiscountPercent(new BigDecimal(itemJson.get("discountPercent").getAsString()));
                    } else {
                        item.setDiscountPercent(BigDecimal.ZERO);
                    }

                    items.add(item);
                }

                invoice.setItems(items);
            } else {
                sendBadRequestResponse(response, "Invoice items are required");
                return;
            }

            // Apply discount if provided
            if (jsonRequest.has("discountAmount")) {
                invoice.setDiscountAmount(new BigDecimal(jsonRequest.get("discountAmount").getAsString()));
            }

            // Apply tax if requested
            boolean applyTax = false;
            if (jsonRequest.has("applyTax")) {
                applyTax = jsonRequest.get("applyTax").getAsBoolean();
            }

            // Calculate bill
            invoice = billingService.calculateBill(invoice, applyTax);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Bill calculated successfully");
            jsonResponse.add("invoice", gson.toJsonTree(invoice));

            out.print(gson.toJson(jsonResponse));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating bill", e);
            sendErrorResponse(response, "Error calculating bill: " + e.getMessage());
            return;
        }

        out.flush();
    }

    /**
     * Handle invoice preview generation without saving to database
     */
    private void handlePreviewRequest(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
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
            JsonObject jsonRequest = JsonParser.parseString(requestBody.toString()).getAsJsonObject();

            // Create invoice object from JSON
            Invoice invoice = new Invoice();

            // Set cashier information
            invoice.setCashierId(user.getId());
            invoice.setCashierName(user.getFullName());

            // Set invoice date - use current timestamp
            invoice.setInvoiceDate(new Timestamp(System.currentTimeMillis()));

            // Set invoice number if provided
            if (jsonRequest.has("invoiceNumber")) {
                invoice.setInvoiceNumber(jsonRequest.get("invoiceNumber").getAsString());
            } else {
                // Generate temporary invoice number for preview
                invoice.setInvoiceNumber("PREVIEW-" + System.currentTimeMillis());
            }

            // Set customer information
            if (jsonRequest.has("customerId") && !jsonRequest.get("customerId").isJsonNull()) {
                invoice.setCustomerId(jsonRequest.get("customerId").getAsInt());
            }

            // Handle customer name if provided
            if (jsonRequest.has("customerName") && !jsonRequest.get("customerName").isJsonNull()) {
                invoice.setCustomerName(jsonRequest.get("customerName").getAsString());
            }

            // Set payment method
            if (jsonRequest.has("paymentMethod") && !jsonRequest.get("paymentMethod").getAsString().isEmpty()) {
                invoice.setPaymentMethod(jsonRequest.get("paymentMethod").getAsString());
            } else {
                invoice.setPaymentMethod("CASH"); // Default
            }

            // Set notes
            if (jsonRequest.has("notes")) {
                invoice.setNotes(jsonRequest.get("notes").getAsString());
            }

            // Parse invoice items
            if (jsonRequest.has("items") && jsonRequest.get("items").isJsonArray()) {
                JsonArray itemsArray = jsonRequest.get("items").getAsJsonArray();
                List<InvoiceItem> items = new ArrayList<>();

                for (int i = 0; i < itemsArray.size(); i++) {
                    JsonObject itemJson = itemsArray.get(i).getAsJsonObject();
                    InvoiceItem item = new InvoiceItem();

                    // Required fields
                    if (itemJson.has("bookId")) {
                        item.setBookId(itemJson.get("bookId").getAsInt());
                    } else {
                        sendBadRequestResponse(response, "Book ID is required for invoice item");
                        return;
                    }

                    if (itemJson.has("quantity")) {
                        item.setQuantity(itemJson.get("quantity").getAsInt());
                    } else {
                        sendBadRequestResponse(response, "Quantity is required for invoice item");
                        return;
                    }

                    // Use book title from request if available (useful for preview)
                    if (itemJson.has("bookTitle") && !itemJson.get("bookTitle").isJsonNull()) {
                        item.setBookTitle(itemJson.get("bookTitle").getAsString());
                    } else {
                        // For preview purposes, use a placeholder
                        item.setBookTitle("Book #" + item.getBookId());
                    }

                    // Set unit price (required for calculations)
                    if (itemJson.has("unitPrice")) {
                        item.setUnitPrice(new BigDecimal(itemJson.get("unitPrice").getAsString()));
                    } else {
                        sendBadRequestResponse(response, "Unit price is required for preview");
                        return;
                    }

                    // Set discount percent
                    if (itemJson.has("discountPercent")) {
                        item.setDiscountPercent(new BigDecimal(itemJson.get("discountPercent").getAsString()));
                    } else {
                        item.setDiscountPercent(BigDecimal.ZERO);
                    }

                    // Calculate item total price
                    item.calculateTotalPrice();
                    items.add(item);
                }

                invoice.setItems(items);
            } else {
                sendBadRequestResponse(response, "Invoice items are required");
                return;
            }

            // Set subtotal if provided
            if (jsonRequest.has("subtotal")) {
                invoice.setSubtotal(new BigDecimal(jsonRequest.get("subtotal").getAsString()));
            }

            // Set discount amount if provided
            if (jsonRequest.has("discountAmount")) {
                invoice.setDiscountAmount(new BigDecimal(jsonRequest.get("discountAmount").getAsString()));
            }

            // Set tax amount if provided
            if (jsonRequest.has("taxAmount")) {
                invoice.setTaxAmount(new BigDecimal(jsonRequest.get("taxAmount").getAsString()));
            }

            // Set total amount if provided
            if (jsonRequest.has("totalAmount")) {
                invoice.setTotalAmount(new BigDecimal(jsonRequest.get("totalAmount").getAsString()));
            }

            // Apply tax if requested (recalculate totals)
            boolean applyTax = false;
            if (jsonRequest.has("applyTax")) {
                applyTax = jsonRequest.get("applyTax").getAsBoolean();
            }

            // Ensure totals are calculated
            if (invoice.getSubtotal() == null || invoice.getTotalAmount() == null) {
                invoice = billingService.calculateBill(invoice, applyTax);
            }

            // Generate printable content
            String printableContent = billingService.generatePrintableBill(invoice);

            // Return success with printable content
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("printableInvoice", printableContent);

            out.print(gson.toJson(jsonResponse));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating invoice preview", e);
            sendErrorResponse(response, "Error generating preview: " + e.getMessage());
        }
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
