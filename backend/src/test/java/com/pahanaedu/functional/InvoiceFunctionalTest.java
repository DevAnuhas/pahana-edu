package com.pahanaedu.functional;

import com.pahanaedu.dao.InvoiceDAO;
import com.pahanaedu.dao.InvoiceItemDAO;
import com.pahanaedu.model.Invoice;
import com.pahanaedu.model.InvoiceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for invoice and billing operations
 */
public class InvoiceFunctionalTest {

    private MockInvoiceDAO mockInvoiceDAO;
    private MockInvoiceItemDAO mockInvoiceItemDAO;

    private static class MockInvoiceDAO extends InvoiceDAO {
        private final List<Invoice> invoices = new ArrayList<>();
        private int nextId = 1;

        @Override
        public Invoice findById(int id) {
            return invoices.stream()
                    .filter(invoice -> invoice.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Invoice> findAll() {
            return new ArrayList<>(invoices);
        }

        @Override
        public boolean create(Invoice invoice) {
            invoice.setId(nextId++);
            invoices.add(invoice);
            return true;
        }
    }

    private static class MockInvoiceItemDAO extends InvoiceItemDAO {
        private final List<InvoiceItem> items = new ArrayList<>();
        private int nextId = 1;

        @Override
        public List<InvoiceItem> findByInvoiceId(int invoiceId) {
            return items.stream()
                    .filter(item -> item.getInvoiceId() == invoiceId)
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public boolean create(InvoiceItem item) {
            item.setId(nextId++);
            items.add(item);
            return true;
        }
    }

    @BeforeEach
    public void setUp() {
        mockInvoiceDAO = new MockInvoiceDAO();
        mockInvoiceItemDAO = new MockInvoiceItemDAO();
    }

    @Test
    public void testCreateValidInvoice() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-2025-001");
        invoice.setCashierId(1);
        invoice.setSubtotal(new BigDecimal("100.00"));
        invoice.setTotalAmount(new BigDecimal("100.00"));

        boolean result = mockInvoiceDAO.create(invoice);
        assertTrue(result);
        assertNotEquals(0, invoice.getId());
    }

    @Test
    public void testCalculateInvoiceTotal() {
        Invoice invoice = new Invoice();
        List<InvoiceItem> items = new ArrayList<>();

        InvoiceItem item1 = new InvoiceItem();
        item1.setTotalPrice(new BigDecimal("100.00"));
        items.add(item1);
        
        invoice.setItems(items);
        invoice.setDiscountAmount(new BigDecimal("10.00"));
        invoice.setTaxAmount(new BigDecimal("5.00"));
        
        invoice.calculateTotals();
        assertEquals(new BigDecimal("95.00"), invoice.getTotalAmount());
    }

    @Test
    public void testAddInvoiceItem() {
        InvoiceItem item = new InvoiceItem();
        item.setInvoiceId(1);
        item.setBookId(1);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));
        item.setTotalPrice(new BigDecimal("50.00"));

        boolean result = mockInvoiceItemDAO.create(item);
        assertTrue(result);
        assertNotEquals(0, item.getId());
    }

    @Test
    public void testValidateInvoiceCalculation() {
        InvoiceItem item1 = new InvoiceItem();
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("25.00"));
        item1.setDiscountPercent(new BigDecimal("10.00"));
        item1.calculateTotalPrice();
        
        // Expected calculation: (25.00 - 2.50) * 2 = 22.50 * 2 = 45.00
        // Use compareTo to handle BigDecimal precision issues
        assertEquals(0, new BigDecimal("45.00").compareTo(item1.getTotalPrice()));
    }

    @Test
    public void testInvoiceWithMultipleItems() {
        Invoice invoice = new Invoice();
        List<InvoiceItem> items = new ArrayList<>();
        
        InvoiceItem item1 = new InvoiceItem();
        item1.setTotalPrice(new BigDecimal("30.00"));
        items.add(item1);
        
        InvoiceItem item2 = new InvoiceItem();
        item2.setTotalPrice(new BigDecimal("20.00"));
        items.add(item2);
        
        invoice.setItems(items);
        invoice.calculateTotals();
        
        assertEquals(new BigDecimal("50.00"), invoice.getSubtotal());
    }
}