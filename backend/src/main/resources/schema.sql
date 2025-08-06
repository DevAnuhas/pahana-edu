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

-- Create purchases table for inventory management
CREATE TABLE IF NOT EXISTS purchases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_number VARCHAR(20) UNIQUE NOT NULL,
    publisher_id INT NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    received_by INT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE RESTRICT,
    FOREIGN KEY (received_by) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_purchases_purchase_number (purchase_number)
);

-- Create purchase_items table
CREATE TABLE IF NOT EXISTS purchase_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
    INDEX idx_purchase_items_purchase_id (purchase_id),
    INDEX idx_purchase_items_book_id (book_id)
);

-- Insert default admin user
INSERT INTO users (username, password, full_name, role, email)
VALUES ('admin', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'System Administrator', 'ADMIN', 'admin@pahanaedu.com')
ON DUPLICATE KEY UPDATE username = 'admin';

-- Insert default categories
INSERT INTO categories (name, description)
VALUES
('Fiction', 'Novels, short stories, and other fictional works'),
('Educational', 'Textbooks and educational materials'),
('Children', 'Books for children and young readers'),
('Self-Help', 'Personal development and self-improvement books'),
('Business', 'Business, economics, and finance books')
ON DUPLICATE KEY UPDATE name = VALUES(name);
