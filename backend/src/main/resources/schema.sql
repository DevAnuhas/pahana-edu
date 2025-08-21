-- Database initialization script for Pahana Edu Bookshop Management System

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS pahana_bookshop;
USE pahana_bookshop;

-- Create users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'CASHIER') NOT NULL,
    email VARCHAR(100) UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    registration_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customers_account_number (account_number),
    INDEX idx_customers_telephone (telephone)
);

-- Create book categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create publishers table
CREATE TABLE IF NOT EXISTS publishers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    contact_person VARCHAR(100),
    telephone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    publisher_id INT NOT NULL,
    publication_year INT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE RESTRICT,
    INDEX idx_books_isbn (isbn),
    INDEX idx_books_title (title),
    INDEX idx_books_author (author),
    INDEX idx_books_category (category_id)
);

-- Create invoices table
CREATE TABLE IF NOT EXISTS invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id INT,
    cashier_id INT NOT NULL,
    invoice_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'ONLINE') DEFAULT 'CASH',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (cashier_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_invoices_invoice_number (invoice_number),
    INDEX idx_invoices_customer_id (customer_id),
    INDEX idx_invoices_invoice_date (invoice_date)
);

-- Create invoice_items table
CREATE TABLE IF NOT EXISTS invoice_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
    INDEX idx_invoice_items_invoice_id (invoice_id),
    INDEX idx_invoice_items_book_id (book_id)
);

-- Insert default admin user
INSERT INTO users (username, password, full_name, role, email)
-- Password: admin1234
VALUES
('admin', 'o7VXRdJ9+T+AVqkJIaCkZA==:7dkfWdM+GgDdMyvVArJTaApO8/fWQZXhpapq71BzDi9JwF8FSbzXnsbJYPX3O1bjLXBcOe1jkVnNJSJgVTviNQ==', 'System Administrator', 'ADMIN', 'admin@pahanaedu.com')
ON DUPLICATE KEY UPDATE username = 'admin';

-- Insert demo users (cashiers)
INSERT INTO users (username, password, full_name, role, email)
VALUES
-- Password: staff1234
('staff1', 'DBAz2Bl/B+oZ/aoDEbyYCg==:R6GqmvQry8tlQ22bZrdFZwa1Nyt+/h08DxFGxyFhYbiZszzZBlb+KgmUKsawjCVsWmNaBVxc2ZxFJ2VsLRQ1wg==', 'Nishanth Perera', 'CASHIER', 'nishanth@pahanaedu.com'),
('staff2', 'DBAz2Bl/B+oZ/aoDEbyYCg==:R6GqmvQry8tlQ22bZrdFZwa1Nyt+/h08DxFGxyFhYbiZszzZBlb+KgmUKsawjCVsWmNaBVxc2ZxFJ2VsLRQ1wg==', 'Dilhani Silva', 'CASHIER', 'dilhani@pahanaedu.com')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Insert default categories
INSERT INTO categories (name, description)
VALUES
    ('Fiction', 'Novels, short stories, and other fictional works'),
    ('Educational', 'Textbooks and educational materials'),
    ('Children', 'Books for children and young readers'),
    ('Self-Help', 'Personal development and self-improvement books'),
    ('Business', 'Business, economics, and finance books')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert demo publishers
INSERT INTO publishers (name, contact_person, telephone, email, address)
VALUES
    ('Academic Press', 'John Williams', '0112847561', 'info@academicpress.com', '123 University Road, Colombo 7'),
    ('Lanka Publications', 'Kumari Silva', '0772568941', 'kumari@lankapub.lk', '45 Temple Road, Kandy'),
    ('Children Books Lanka', 'Amal Perera', '0812365478', 'amal@childrenbooks.lk', '78 Hill Street, Nuwara Eliya'),
    ('Business Knowledge Ltd', 'Dinesh Fernando', '0114589632', 'sales@businessknowledge.lk', '256 Galle Road, Colombo 3'),
    ('Serendib Publishing', 'Malini Gunawardena', '0765478123', 'contact@serendibpub.com', '32 Beach Road, Negombo')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert demo customers
INSERT INTO customers (account_number, name, address, telephone, email, registration_date)
VALUES
    ('CUS001', 'Sampath Jayaweera', '123 Main St, Colombo 5', '0771234567', 'sampath@email.com', '2024-01-15'),
    ('CUS002', 'Kumari Perera', '45 Lake Road, Kandy', '0712345678', 'kumari@email.com', '2024-02-20'),
    ('CUS003', 'Lakmal Fernando', '67 Beach Road, Galle', '0761234567', 'lakmal@email.com', '2024-03-10'),
    ('CUS004', 'Priyanka Silva', '89 Hill St, Nuwara Eliya', '0701234567', 'priyanka@email.com', '2024-04-05'),
    ('CUS005', 'Nimal Gunaratne', '12 Temple Road, Matara', '0751234567', 'nimal@email.com', '2024-05-15'),
    ('CUS006', 'Dilini Bandara', '34 Park Avenue, Colombo 7', '0781234567', 'dilini@email.com', '2024-06-22'),
    ('CUS007', 'Chaminda Rajapaksa', '56 Forest Lane, Anuradhapura', '0721234567', 'chaminda@email.com', '2024-07-18'),
    ('CUS008', 'Sanduni Wickramasinghe', '78 Ocean View, Trincomalee', '0731234567', 'sanduni@email.com', '2024-08-01')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert demo books
INSERT INTO books (isbn, title, author, category_id, publisher_id, publication_year, price, stock_quantity, description)
VALUES
    ('978-1-234567-89-0', 'Advanced Mathematics Grade 12', 'Prof. Ranjith Silva', 2, 1, 2024, 2500.00, 45, 'Comprehensive mathematics textbook for Grade 12 students preparing for A/Level examinations.'),
    ('978-1-345678-90-1', 'English Grammar Essentials', 'Dr. Sarah Johnson', 2, 1, 2023, 1800.00, 32, 'Essential grammar guide for English language learners with practice exercises.'),
    ('978-1-456789-01-2', 'The Hidden Village', 'Lasantha Wickramasinghe', 1, 2, 2024, 1200.00, 25, 'A thrilling mystery novel set in a remote Sri Lankan village.'),
    ('978-1-567890-12-3', 'Sinhala Poetry Collection', 'Various Authors', 1, 2, 2022, 950.00, 15, 'Collection of classic and contemporary Sinhala poetry.'),
    ('978-1-678901-23-4', 'Bedtime Stories for Children', 'Amali Fernando', 3, 3, 2023, 850.00, 40, 'Illustrated collection of bedtime stories for young children.'),
    ('978-1-789012-34-5', 'Animal Adventures', 'Chamara Perera', 3, 3, 2024, 750.00, 0, 'Interactive story book about animal adventures for children aged 3-6.'),
    ('978-1-890123-45-6', 'Mindfulness for Beginners', 'Dr. Kumari Jayasinghe', 4, 5, 2023, 1500.00, 8, 'Introduction to mindfulness practice with guided exercises.'),
    ('978-1-901234-56-7', 'Personal Finance 101', 'Nimal Gunawardena', 4, 4, 2022, 1750.00, 20, 'Basic guide to personal finance management for young adults.'),
    ('978-1-012345-67-8', 'Business Leadership', 'Prof. Dinesh Ranatunga', 5, 4, 2024, 2200.00, 12, 'Modern approaches to business leadership and management.'),
    ('978-1-123456-78-9', 'Entrepreneurship in Sri Lanka', 'Samantha Fernando', 5, 4, 2023, 1950.00, 18, 'Case studies of successful entrepreneurs in the Sri Lankan context.')
ON DUPLICATE KEY UPDATE isbn = VALUES(isbn);

-- Insert demo invoices
INSERT INTO invoices (invoice_number, customer_id, cashier_id, invoice_date, subtotal, discount_amount, tax_amount, total_amount, payment_method, notes)
VALUES
    ('INV-2025-001', 1, 2, '2025-07-01 10:15:00', 4300.00, 0.00, 0.00, 4300.00, 'CASH', 'Regular customer purchase'),
    ('INV-2025-002', 2, 3, '2025-07-03 14:30:00', 2500.00, 250.00, 0.00, 2250.00, 'CARD', '10% discount applied'),
    ('INV-2025-003', 3, 2, '2025-07-05 09:45:00', 1200.00, 0.00, 0.00, 1200.00, 'CASH', NULL),
    ('INV-2025-004', 4, 3, '2025-07-10 16:20:00', 3250.00, 325.00, 0.00, 2925.00, 'CARD', '10% discount for educational books'),
    ('INV-2025-005', 5, 2, '2025-07-15 11:30:00', 1700.00, 0.00, 0.00, 1700.00, 'CASH', 'First-time customer'),
    ('INV-2025-006', 6, 3, '2025-07-18 14:15:00', 4150.00, 415.00, 0.00, 3735.00, 'ONLINE', '10% discount applied'),
    ('INV-2025-007', 7, 2, '2025-07-22 10:45:00', 2700.00, 0.00, 0.00, 2700.00, 'CASH', NULL),
    ('INV-2025-008', 8, 3, '2025-07-25 15:30:00', 5200.00, 520.00, 0.00, 4680.00, 'CARD', 'Bulk purchase discount')
ON DUPLICATE KEY UPDATE invoice_number = VALUES(invoice_number);

-- Insert demo invoice items
INSERT INTO invoice_items (invoice_id, book_id, quantity, unit_price, discount_percent, total_price)
VALUES
    (1, 1, 1, 2500.00, 0.00, 2500.00),
    (1, 2, 1, 1800.00, 0.00, 1800.00),
    (2, 1, 1, 2500.00, 10.00, 2250.00),
    (3, 3, 1, 1200.00, 0.00, 1200.00),
    (4, 2, 1, 1800.00, 10.00, 1620.00),
    (4, 4, 1, 950.00, 0.00, 950.00),
    (4, 5, 1, 850.00, 20.00, 680.00),
    (5, 7, 1, 1500.00, 0.00, 1500.00),
    (5, 8, 1, 200.00, 0.00, 200.00),
    (6, 9, 1, 2200.00, 10.00, 1980.00),
    (6, 10, 1, 1950.00, 10.00, 1755.00),
    (7, 5, 2, 850.00, 0.00, 1700.00),
    (7, 1, 1, 2500.00, 60.00, 1000.00),
    (8, 1, 1, 2500.00, 10.00, 2250.00),
    (8, 9, 1, 2200.00, 10.00, 1980.00),
    (8, 3, 1, 1200.00, 20.00, 960.00);

    
-- =====================================================
-- DATABASE TRIGGERS FOR BUSINESS RULE ENFORCEMENT
-- =====================================================

-- Trigger to automatically update book stock when invoice items are added
DELIMITER $$

CREATE TRIGGER tr_update_stock_on_sale
    AFTER INSERT ON invoice_items
    FOR EACH ROW
BEGIN
    -- Update book stock by reducing the sold quantity
    UPDATE books 
    SET stock_quantity = stock_quantity - NEW.quantity,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.book_id;
    
    -- Check if stock goes negative and raise error if insufficient stock
    IF (SELECT stock_quantity FROM books WHERE id = NEW.book_id) < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Insufficient stock for this book. Transaction cannot be completed.';
    END IF;
END$$

-- Trigger to restore stock when invoice items are deleted (returns/cancellations)
CREATE TRIGGER tr_restore_stock_on_return
    AFTER DELETE ON invoice_items
    FOR EACH ROW
BEGIN
    -- Restore book stock by adding back the returned quantity
    UPDATE books 
    SET stock_quantity = stock_quantity + OLD.quantity,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = OLD.book_id;
END$$

-- Trigger to handle stock adjustments when invoice items are updated
CREATE TRIGGER tr_adjust_stock_on_update
    AFTER UPDATE ON invoice_items
    FOR EACH ROW
BEGIN
    -- Calculate the difference in quantity
    DECLARE quantity_diff INT;
    SET quantity_diff = NEW.quantity - OLD.quantity;
    
    -- Adjust stock based on quantity difference
    UPDATE books 
    SET stock_quantity = stock_quantity - quantity_diff,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.book_id;
    
    -- Check if stock goes negative
    IF (SELECT stock_quantity FROM books WHERE id = NEW.book_id) < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Insufficient stock for this quantity adjustment.';
    END IF;
END$$

-- Trigger to validate invoice item calculations
CREATE TRIGGER tr_validate_invoice_item_calculations
    BEFORE INSERT ON invoice_items
    FOR EACH ROW
BEGIN
    DECLARE calculated_total DECIMAL(10,2);
    
    -- Calculate the expected total price
    SET calculated_total = NEW.unit_price * NEW.quantity * (1 - NEW.discount_percent / 100);
    
    -- Validate the total price calculation
    IF ABS(NEW.total_price - calculated_total) > 0.01 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Invoice item total price calculation is incorrect.';
    END IF;
    
    -- Ensure quantity is positive
    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Quantity must be greater than zero.';
    END IF;
    
    -- Ensure unit price is positive
    IF NEW.unit_price <= 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Unit price must be greater than zero.';
    END IF;
    
    -- Validate discount percentage
    IF NEW.discount_percent < 0 OR NEW.discount_percent > 100 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Discount percentage must be between 0 and 100.';
    END IF;
END$$

-- Trigger to automatically calculate invoice totals
CREATE TRIGGER tr_calculate_invoice_totals
    AFTER INSERT ON invoice_items
    FOR EACH ROW
BEGIN
    DECLARE calculated_subtotal DECIMAL(10,2);
    DECLARE calculated_total DECIMAL(10,2);
    
    -- Calculate subtotal from all invoice items
    SELECT COALESCE(SUM(total_price), 0) INTO calculated_subtotal
    FROM invoice_items 
    WHERE invoice_id = NEW.invoice_id;
    
    -- Calculate final total (subtotal - discount + tax)
    SELECT (calculated_subtotal - COALESCE(discount_amount, 0) + COALESCE(tax_amount, 0)) 
    INTO calculated_total
    FROM invoices 
    WHERE id = NEW.invoice_id;
    
    -- Update invoice with calculated totals
    UPDATE invoices 
    SET subtotal = calculated_subtotal,
        total_amount = calculated_total
    WHERE id = NEW.invoice_id;
END$$

-- Trigger to recalculate invoice totals when items are updated
CREATE TRIGGER tr_recalculate_invoice_totals_on_update
    AFTER UPDATE ON invoice_items
    FOR EACH ROW
BEGIN
    DECLARE calculated_subtotal DECIMAL(10,2);
    DECLARE calculated_total DECIMAL(10,2);
    
    -- Calculate subtotal from all invoice items
    SELECT COALESCE(SUM(total_price), 0) INTO calculated_subtotal
    FROM invoice_items 
    WHERE invoice_id = NEW.invoice_id;
    
    -- Calculate final total
    SELECT (calculated_subtotal - COALESCE(discount_amount, 0) + COALESCE(tax_amount, 0)) 
    INTO calculated_total
    FROM invoices 
    WHERE id = NEW.invoice_id;
    
    -- Update invoice with recalculated totals
    UPDATE invoices 
    SET subtotal = calculated_subtotal,
        total_amount = calculated_total
    WHERE id = NEW.invoice_id;
END$$

-- Trigger to recalculate invoice totals when items are deleted
CREATE TRIGGER tr_recalculate_invoice_totals_on_delete
    AFTER DELETE ON invoice_items
    FOR EACH ROW
BEGIN
    DECLARE calculated_subtotal DECIMAL(10,2);
    DECLARE calculated_total DECIMAL(10,2);
    
    -- Calculate subtotal from remaining invoice items
    SELECT COALESCE(SUM(total_price), 0) INTO calculated_subtotal
    FROM invoice_items 
    WHERE invoice_id = OLD.invoice_id;
    
    -- Calculate final total
    SELECT (calculated_subtotal - COALESCE(discount_amount, 0) + COALESCE(tax_amount, 0)) 
    INTO calculated_total
    FROM invoices 
    WHERE id = OLD.invoice_id;
    
    -- Update invoice with recalculated totals
    UPDATE invoices 
    SET subtotal = calculated_subtotal,
        total_amount = calculated_total
    WHERE id = OLD.invoice_id;
END$$

-- Trigger to prevent deletion of books that have been sold
CREATE TRIGGER tr_prevent_book_deletion_if_sold
    BEFORE DELETE ON books
    FOR EACH ROW
BEGIN
    DECLARE sold_count INT;
    
    -- Check if book has been sold
    SELECT COUNT(*) INTO sold_count
    FROM invoice_items 
    WHERE book_id = OLD.id;
    
    IF sold_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Cannot delete book that has been sold. Consider marking as inactive instead.';
    END IF;
END$$

-- Trigger to validate customer account number format
CREATE TRIGGER tr_validate_customer_account_format
    BEFORE INSERT ON customers
    FOR EACH ROW
BEGIN
    -- Validate account number format (must start with 'CUS' followed by 3 digits)
    IF NEW.account_number NOT REGEXP '^CUS[0-9]{3}$' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Customer account number must follow format: CUS###';
    END IF;
    
    -- Validate telephone number format (Sri Lankan format)
    IF NEW.telephone NOT REGEXP '^07[0-9]{8}$' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Telephone number must be in Sri Lankan format: 07########';
    END IF;
END$$

-- Trigger to generate unique invoice numbers
CREATE TRIGGER tr_generate_invoice_number
    BEFORE INSERT ON invoices
    FOR EACH ROW
BEGIN
    DECLARE next_number INT;
    DECLARE year_part VARCHAR(4);
    DECLARE new_invoice_number VARCHAR(20);
    
    -- Get current year
    SET year_part = YEAR(CURRENT_DATE);
    
    -- Get next invoice number for the year
    SELECT COALESCE(MAX(CAST(SUBSTRING(invoice_number, 10) AS UNSIGNED)), 0) + 1 
    INTO next_number
    FROM invoices 
    WHERE invoice_number LIKE CONCAT('INV-', year_part, '-%');
    
    -- Generate new invoice number
    SET new_invoice_number = CONCAT('INV-', year_part, '-', LPAD(next_number, 3, '0'));
    
    -- Set the generated invoice number if not provided
    IF NEW.invoice_number IS NULL OR NEW.invoice_number = '' THEN
        SET NEW.invoice_number = new_invoice_number;
    END IF;
END$$

-- Trigger to log stock level warnings
CREATE TRIGGER tr_stock_level_warning
    AFTER UPDATE ON books
    FOR EACH ROW
BEGIN
    -- Log warning when stock falls below minimum threshold (5 books)
    IF NEW.stock_quantity <= 5 AND NEW.stock_quantity > 0 THEN
        INSERT INTO stock_alerts (book_id, alert_type, message, created_at)
        VALUES (NEW.id, 'LOW_STOCK', 
                CONCAT('Stock level for "', NEW.title, '" is low: ', NEW.stock_quantity, ' remaining'), 
                CURRENT_TIMESTAMP);
    END IF;
    
    -- Log critical warning when stock is depleted
    IF NEW.stock_quantity = 0 THEN
        INSERT INTO stock_alerts (book_id, alert_type, message, created_at)
        VALUES (NEW.id, 'OUT_OF_STOCK', 
                CONCAT('Book "', NEW.title, '" is out of stock'), 
                CURRENT_TIMESTAMP);
    END IF;
END$$

DELIMITER ;

-- Create stock alerts table for tracking inventory warnings
CREATE TABLE IF NOT EXISTS stock_alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    alert_type ENUM('LOW_STOCK', 'OUT_OF_STOCK', 'RESTOCK_NEEDED') NOT NULL,
    message TEXT NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_stock_alerts_book_id (book_id),
    INDEX idx_stock_alerts_type (alert_type),
    INDEX idx_stock_alerts_resolved (is_resolved)
);

-- Create audit log table for tracking important database changes
CREATE TABLE IF NOT EXISTS audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    operation ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    record_id INT NOT NULL,
    old_values JSON,
    new_values JSON,
    user_id INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    INDEX idx_audit_table_name (table_name),
    INDEX idx_audit_operation (operation),
    INDEX idx_audit_timestamp (timestamp),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);