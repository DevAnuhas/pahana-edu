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
('admin', '$2a$10$W9SVm2Fy3ziFLwuasTa.k.BzY95HYim8uHpiOGUfj57sM7j9sYhFK', 'System Administrator', 'ADMIN', 'admin@pahanaedu.com')
ON DUPLICATE KEY UPDATE username = 'admin';

-- Insert demo users (cashiers)
INSERT INTO users (username, password, full_name, role, email)
VALUES
-- Password: staff1234
('staff1', '$2a$10$O5LhfbyITmiFIgpLOyNiZuFameXDzacdmk/SzrSpoCuADtragtZhC', 'Nishanth Perera', 'CASHIER', 'nishanth@pahanaedu.com'),
('staff2', '$2a$10$O5LhfbyITmiFIgpLOyNiZuFameXDzacdmk/SzrSpoCuADtragtZhC', 'Dilhani Silva', 'CASHIER', 'dilhani@pahanaedu.com')
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
    ('Children\'s Books Lanka', 'Amal Perera', '0812365478', 'amal@childrenbooks.lk', '78 Hill Street, Nuwara Eliya'),
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
