package com.pahanaedu.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * InvoiceItem model representing an item in an invoice
 */
public class InvoiceItem {
    private int id;
    private int invoiceId;
    private int bookId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal totalPrice;
    private Timestamp createdAt;

    // For UI display
    private String bookTitle;
    private String bookIsbn;

    public InvoiceItem() {
        this.discountPercent = BigDecimal.ZERO;
    }

    public InvoiceItem(int bookId, int quantity, BigDecimal unitPrice, BigDecimal discountPercent) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        this.calculateTotalPrice();
    }

    public InvoiceItem(int id, int invoiceId, int bookId, int quantity, BigDecimal unitPrice,
                      BigDecimal discountPercent, BigDecimal totalPrice, Timestamp createdAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    // Helper method to calculate total price
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity > 0) {
            BigDecimal discount = unitPrice.multiply(discountPercent).divide(new BigDecimal("100"));
            BigDecimal priceAfterDiscount = unitPrice.subtract(discount);
            this.totalPrice = priceAfterDiscount.multiply(new BigDecimal(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", bookId=" + bookId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", discountPercent=" + discountPercent +
                ", totalPrice=" + totalPrice +
                ", bookTitle='" + bookTitle + '\'' +
                '}';
    }
}
