package com.pahanaedu.service;

import com.pahanaedu.dao.BookDAO;
import com.pahanaedu.dao.InvoiceDAO;
import com.pahanaedu.dao.InvoiceItemDAO;
import com.pahanaedu.model.Book;
import com.pahanaedu.model.Invoice;
import com.pahanaedu.model.InvoiceItem;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for handling billing and invoice-related business logic
 */
public class BillingService {
    private static final Logger LOGGER = Logger.getLogger(BillingService.class.getName());
    private final InvoiceDAO invoiceDAO;
    private final InvoiceItemDAO invoiceItemDAO;
    private final BookDAO bookDAO;

    public BillingService() {
        this.invoiceDAO = new InvoiceDAO();
        this.invoiceItemDAO = new InvoiceItemDAO();
        this.bookDAO = new BookDAO();
    }

    public Invoice getInvoiceById(int id) {
        LOGGER.info("Fetching invoice with ID: " + id);
        return invoiceDAO.findById(id);
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        LOGGER.info("Fetching invoice with number: " + invoiceNumber);
        return invoiceDAO.findByInvoiceNumber(invoiceNumber);
    }

    public List<Invoice> getAllInvoices() {
        LOGGER.info("Fetching all invoices");
        return invoiceDAO.findAll();
    }

    public List<Invoice> getInvoicesByCustomer(int customerId) {
        LOGGER.info("Fetching invoices for customer ID: " + customerId);
        return invoiceDAO.findByCustomerId(customerId);
    }

    public boolean createInvoice(Invoice invoice) {
        LOGGER.info("Creating new invoice with " + (invoice.getItems() != null ? invoice.getItems().size() : 0) + " items");

        // Validate items
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            LOGGER.warning("Cannot create invoice with no items");
            return false;
        }

        // Validate and prepare invoice items
        for (InvoiceItem item : invoice.getItems()) {
            LOGGER.info("Processing item with bookId: " + item.getBookId() + ", quantity: " + item.getQuantity());

            Book book = bookDAO.findById(item.getBookId());

            if (book == null) {
                LOGGER.severe("CRITICAL ERROR: Book not found with ID: " + item.getBookId());
                return false;
            }

            LOGGER.info("Book found: '" + book.getTitle() + "' (ID: " + book.getId() +
                       "), Current Stock: " + book.getStockQuantity() +
                       ", Requested Quantity: " + item.getQuantity() +
                       ", Book Price: $" + book.getPrice());

            // Check stock availability
            if (book.getStockQuantity() < item.getQuantity()) {
                LOGGER.severe("INSUFFICIENT STOCK: Book '" + book.getTitle() + "' (ID: " + item.getBookId() +
                             ") - Available: " + book.getStockQuantity() +
                             ", Requested: " + item.getQuantity() +
                             ", Shortage: " + (item.getQuantity() - book.getStockQuantity()));
                return false;
            }

            // Set item details if not already set
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(book.getPrice());
                LOGGER.info("Set unit price from book: $" + book.getPrice());
            }

            // Set book title for display
            item.setBookTitle(book.getTitle());
            item.setBookIsbn(book.getIsbn());

            // Calculate total price
            item.calculateTotalPrice();
            LOGGER.info("Item total calculated: $" + item.getTotalPrice());
        }

        LOGGER.info("All items validated successfully, proceeding with invoice creation");

        // Calculate invoice totals
        invoice.calculateTotals();
        LOGGER.info("Invoice totals - Subtotal: $" + invoice.getSubtotal() +
                   ", Total: $" + invoice.getTotalAmount());

        // Generate invoice number if not provided
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
            String generatedNumber = invoiceDAO.generateInvoiceNumber();
            invoice.setInvoiceNumber(generatedNumber);
            LOGGER.info("Generated invoice number: " + generatedNumber);
        } else {
            LOGGER.info("Using provided invoice number: " + invoice.getInvoiceNumber());
        }

        // Attempt to create the invoice in database
        boolean result = invoiceDAO.create(invoice);
        LOGGER.info("Invoice creation result: " + (result ? "SUCCESS" : "FAILED"));

        if (result) {
            LOGGER.info("Invoice created successfully with ID: " + invoice.getId());
        } else {
            LOGGER.severe("Failed to create invoice in database");
        }

        return result;
    }

    public boolean deleteInvoice(int invoiceId) {
        LOGGER.info("Deleting invoice with ID: " + invoiceId);
        return invoiceDAO.delete(invoiceId);
    }

    /**
     * Calculate bill amount based on items and business rules
     * @param invoice Invoice with items
     * @param applyTax Whether to apply tax (usually 5%)
     * @return Updated invoice with calculated totals
     */
    public Invoice calculateBill(Invoice invoice, boolean applyTax) {
        LOGGER.info("Calculating bill for invoice");

        // Calculate item totals
        for (InvoiceItem item : invoice.getItems()) {
            item.calculateTotalPrice();
        }

        // Calculate invoice subtotal
        invoice.calculateTotals();

        // Apply tax if required (assume 5% tax rate)
        if (applyTax) {
            BigDecimal taxRate = new BigDecimal("0.05");  // 5% tax
            BigDecimal taxAmount = invoice.getSubtotal().multiply(taxRate);
            invoice.setTaxAmount(taxAmount);

            // Recalculate total with tax
            invoice.setTotalAmount(invoice.getSubtotal().subtract(invoice.getDiscountAmount()).add(taxAmount));
        }

        return invoice;
    }

    /**
     * Apply discount to invoice
     * @param invoice Invoice to apply discount to
     * @param discountAmount Amount to discount
     * @return Updated invoice with discount applied
     */
    public Invoice applyDiscount(Invoice invoice, BigDecimal discountAmount) {
        LOGGER.info("Applying discount of " + discountAmount + " to invoice");

        if (discountAmount.compareTo(invoice.getSubtotal()) > 0) {
            // Discount cannot be more than subtotal
            LOGGER.warning("Discount amount exceeds invoice subtotal");
            discountAmount = invoice.getSubtotal();
        }

        invoice.setDiscountAmount(discountAmount);
        invoice.calculateTotals();

        return invoice;
    }

    /**
     * Generate a printable representation of the invoice
     * @param invoice Invoice to print
     * @return String representation of the invoice for printing
     */
    public String generatePrintableBill(Invoice invoice) {
        LOGGER.info("Generating printable bill for invoice: " + invoice.getInvoiceNumber());

        List<InvoiceItem> uniqueItems = new ArrayList<>();
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                boolean isDuplicate = false;
                for (InvoiceItem existingItem : uniqueItems) {
                    if (existingItem.getBookId() == item.getBookId()) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    uniqueItems.add(item);
                }
            }
        }

        StringBuilder bill = new StringBuilder();
        bill.append("==================================================\n");
        bill.append("               PAHANA EDU BOOKSHOP              \n");
        bill.append("==================================================\n\n");
        bill.append("Invoice #: ").append(invoice.getInvoiceNumber()).append("\n");
        
        String formattedDate = formatDateWithLocalTime(invoice.getInvoiceDate());
        bill.append("Date: ").append(formattedDate).append("\n");

        if (invoice.getCustomerId() != null && invoice.getCustomerName() != null) {
            bill.append("Customer: ").append(invoice.getCustomerName()).append("\n");
        } else {
            bill.append("Customer: Walk-in Customer\n");
        }

        bill.append("Cashier: ").append(invoice.getCashierName()).append("\n");
        bill.append("\n--------------------------------------------------\n");
        bill.append("Item                      Qty    Price     Total\n");
        bill.append("--------------------------------------------------\n");

        for (InvoiceItem item : uniqueItems) {
            String title = item.getBookTitle();
            if (title.length() > 22) {
                title = title.substring(0, 22) + ".";
            } else {
                StringBuilder paddedTitle = new StringBuilder(title);
                while (paddedTitle.length() < 23) {
                    paddedTitle.append(" ");
                }
                title = paddedTitle.toString();
            }
            
            String price = String.format("%.2f", item.getUnitPrice());
            String total = String.format("%.2f", item.getTotalPrice());
            
            String quantityStr = String.format("%4s", item.getQuantity());
            String priceStr = String.format("%10s", price);
            String totalStr = String.format("%8s", total);
            
            bill.append(title).append(" ").append(quantityStr).append(" ").append(priceStr).append("  ").append(totalStr).append("\n");
        }

        bill.append("--------------------------------------------------\n");
        
        String subtotalStr = String.format("%.2f", invoice.getSubtotal());
        bill.append(String.format("Subtotal:                        %8s\n", subtotalStr));

        if (invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            String discountStr = String.format("%.2f", invoice.getDiscountAmount());
            bill.append(String.format("Discount:                        %8s\n", discountStr));
        }

        if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            String taxStr = String.format("%.2f", invoice.getTaxAmount());
            bill.append(String.format("Tax (5%%):                        %8s\n", taxStr));
        }

        String totalStr = String.format("%.2f", invoice.getTotalAmount());
        bill.append(String.format("TOTAL:                           %8s\n", totalStr));
        bill.append("==================================================\n");
        bill.append("           Thank You For Your Purchase           \n");
        bill.append("==================================================\n");

        return bill.toString();
    }

    /**
     * Utility method to format dates in a user-friendly way with time zone adjustment
     */
    private String formatDateWithLocalTime(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        
        java.util.TimeZone timezone = java.util.TimeZone.getTimeZone("Asia/Kolkata");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        sdf.setTimeZone(timezone);
        return sdf.format(timestamp);
    }
}
