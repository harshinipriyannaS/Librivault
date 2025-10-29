-- LibriVault Database Setup Script
-- Run this script to create the database and all tables with initial data

-- Create database
CREATE DATABASE IF NOT EXISTS librivault_db;
USE librivault_db;

-- Drop existing tables if they exist (for clean setup)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS fines;
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS borrow_requests;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS librarians;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'LIBRARIAN', 'READER') NOT NULL DEFAULT 'READER',
    reader_credits INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (active)
);

-- Librarians table
CREATE TABLE librarians (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    department VARCHAR(100),
    hire_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_employee_id (employee_id),
    INDEX idx_active (active)
);

-- Categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    assigned_librarian_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_librarian_id) REFERENCES librarians(id) ON SET NULL,
    INDEX idx_name (name),
    INDEX idx_active (active)
);

-- Books table
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    description TEXT,
    category_id BIGINT NOT NULL,
    s3_uri VARCHAR(500),
    cover_image_uri VARCHAR(500),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    published_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_isbn (isbn),
    INDEX idx_category (category_id),
    INDEX idx_active (active),
    INDEX idx_available (available_copies)
);

-- Subscriptions table
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('FREE', 'PREMIUM') NOT NULL DEFAULT 'FREE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    book_limit INT NOT NULL DEFAULT 2,
    duration_days INT NOT NULL DEFAULT 30,
    daily_fine_amount DECIMAL(10,2) NOT NULL DEFAULT 10.00,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_active (active),
    INDEX idx_end_date (end_date)
);

-- Payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'CANCELED') NOT NULL DEFAULT 'PENDING',
    payment_type ENUM('SUBSCRIPTION', 'FINE') NOT NULL,
    description TEXT,
    receipt_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_stripe_payment_intent_id (stripe_payment_intent_id),
    INDEX idx_status (status),
    INDEX idx_payment_type (payment_type),
    INDEX idx_created_at (created_at)
);

-- Borrow Requests table
CREATE TABLE borrow_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reader_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    librarian_id BIGINT,
    status ENUM('PENDING', 'APPROVED', 'DECLINED') NOT NULL DEFAULT 'PENDING',
    request_message TEXT,
    response_message TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    FOREIGN KEY (reader_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (librarian_id) REFERENCES users(id) ON SET NULL,
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_id (book_id),
    INDEX idx_librarian_id (librarian_id),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at)
);

-- Borrow Records table
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reader_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrowed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    returned_at TIMESTAMP NULL,
    status ENUM('ACTIVE', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'ACTIVE',
    used_credit BOOLEAN NOT NULL DEFAULT FALSE,
    credits_earned INT NOT NULL DEFAULT 0,
    FOREIGN KEY (reader_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    INDEX idx_borrowed_at (borrowed_at),
    INDEX idx_returned_at (returned_at)
);

-- Fines table
CREATE TABLE fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_record_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    days_overdue INT NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'PAID', 'WAIVED') NOT NULL DEFAULT 'PENDING',
    payment_id BIGINT NULL,
    waived_by BIGINT NULL,
    waived_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (borrow_record_id) REFERENCES borrow_records(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON SET NULL,
    FOREIGN KEY (waived_by) REFERENCES users(id) ON SET NULL,
    INDEX idx_borrow_record_id (borrow_record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_payment_id (payment_id),
    INDEX idx_created_at (created_at)
);

-- Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    type ENUM('WELCOME', 'BORROW_APPROVED', 'BORROW_DECLINED', 'BOOK_DUE_REMINDER', 'BOOK_OVERDUE', 'PAYMENT_SUCCESS', 'SUBSCRIPTION_EXPIRY', 'CREDITS_EARNED', 'ROLE_CHANGED', 'FINE_WAIVED', 'SYSTEM_ANNOUNCEMENT') NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);

-- Insert initial data

-- Default users (passwords are bcrypt hashed)
-- admin@librivault.com / admin123
-- librarian1@librivault.com / librarian123
-- librarian2@librivault.com / librarian123
-- reader1@librivault.com / reader123
-- reader2@librivault.com / reader123
-- reader3@librivault.com / reader123

INSERT INTO users (id, email, password, first_name, last_name, role, reader_credits, active) VALUES
(1, 'admin@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System', 'Administrator', 'ADMIN', 0, TRUE),
(2, 'librarian1@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John', 'Smith', 'LIBRARIAN', 0, TRUE),
(3, 'librarian2@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah', 'Johnson', 'LIBRARIAN', 0, TRUE),
(4, 'reader1@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Alice', 'Brown', 'READER', 2, TRUE),
(5, 'reader2@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Bob', 'Wilson', 'READER', 0, TRUE),
(6, 'reader3@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Carol', 'Davis', 'READER', 1, TRUE);

-- Librarian records
INSERT INTO librarians (id, user_id, employee_id, department, hire_date, active) VALUES
(1, 2, 'LIB001', 'Fiction & Literature', '2023-01-15', TRUE),
(2, 3, 'LIB002', 'Science & Technology', '2023-02-20', TRUE);

-- Book categories
INSERT INTO categories (id, name, description, assigned_librarian_id, active) VALUES
(1, 'Fiction', 'Novels, short stories, and other fictional works', 1, TRUE),
(2, 'Non-Fiction', 'Biographies, memoirs, and factual books', 1, TRUE),
(3, 'Science & Technology', 'Computer science, engineering, and scientific research', 2, TRUE),
(4, 'Business & Economics', 'Business strategy, economics, and finance', 2, TRUE),
(5, 'History', 'Historical events, periods, and figures', 1, TRUE),
(6, 'Self-Help', 'Personal development and improvement books', 1, TRUE),
(7, 'Education', 'Academic textbooks and educational materials', 2, TRUE),
(8, 'Arts & Culture', 'Art, music, literature, and cultural studies', 1, TRUE);

-- Sample books (with placeholder S3 URIs - replace with actual URIs when books are uploaded)
INSERT INTO books (id, title, author, isbn, description, category_id, s3_uri, total_copies, available_copies, published_date, active) VALUES
(1, 'The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 'A classic American novel set in the Jazz Age', 1, 's3://librivault-book-storage/books/the-great-gatsby.pdf', 3, 2, '1925-04-10', TRUE),
(2, 'To Kill a Mockingbird', 'Harper Lee', '9780061120084', 'A gripping tale of racial injustice and childhood innocence', 1, 's3://librivault-book-storage/books/to-kill-a-mockingbird.pdf', 2, 2, '1960-07-11', TRUE),
(3, 'Clean Code', 'Robert C. Martin', '9780132350884', 'A handbook of agile software craftsmanship', 3, 's3://librivault-book-storage/books/clean-code.pdf', 5, 3, '2008-08-01', TRUE),
(4, 'The Lean Startup', 'Eric Ries', '9780307887894', 'How todays entrepreneurs use continuous innovation', 4, 's3://librivault-book-storage/books/the-lean-startup.pdf', 3, 3, '2011-09-13', TRUE),
(5, 'Sapiens', 'Yuval Noah Harari', '9780062316097', 'A brief history of humankind', 2, 's3://librivault-book-storage/books/sapiens.pdf', 4, 3, '2014-02-10', TRUE),
(6, 'The 7 Habits of Highly Effective People', 'Stephen R. Covey', '9781982137274', 'Powerful lessons in personal change', 6, 's3://librivault-book-storage/books/7-habits.pdf', 2, 2, '1989-08-15', TRUE),
(7, 'Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 'Comprehensive introduction to algorithms', 7, 's3://librivault-book-storage/books/intro-algorithms.pdf', 3, 2, '2009-07-31', TRUE),
(8, 'The Art of War', 'Sun Tzu', '9781599869773', 'Ancient Chinese military treatise', 5, 's3://librivault-book-storage/books/art-of-war.pdf', 2, 1, '2006-03-07', TRUE);

-- Default subscriptions
INSERT INTO subscriptions (user_id, type, start_date, end_date, book_limit, duration_days, daily_fine_amount, price, active) VALUES
(1, 'PREMIUM', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY), 200, 300, 5.00, 0.00, TRUE),
(2, 'PREMIUM', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY), 200, 300, 5.00, 0.00, TRUE),
(3, 'PREMIUM', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY), 200, 300, 5.00, 0.00, TRUE),
(4, 'FREE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 2, 30, 10.00, 0.00, TRUE),
(5, 'PREMIUM', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY), 200, 300, 5.00, 599.00, TRUE),
(6, 'FREE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 2, 30, 10.00, 0.00, TRUE);

-- Sample borrow requests
INSERT INTO borrow_requests (id, reader_id, book_id, librarian_id, status, request_message, response_message, requested_at, responded_at) VALUES
(1, 4, 1, 2, 'APPROVED', 'I would like to read this classic novel.', 'Request approved. Enjoy reading!', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 5, 3, 3, 'APPROVED', 'Need this for my software development project.', 'Approved for your project work.', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(3, 6, 4, 2, 'PENDING', 'Interested in learning about startup methodologies.', NULL, NOW(), NULL);

-- Sample borrow records
INSERT INTO borrow_records (id, reader_id, book_id, borrowed_at, due_date, returned_at, status, used_credit, credits_earned) VALUES
(1, 4, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 29 DAY), NULL, 'ACTIVE', FALSE, 0),
(2, 5, 3, NOW(), DATE_ADD(NOW(), INTERVAL 299 DAY), NULL, 'ACTIVE', FALSE, 0);

-- Welcome notifications
INSERT INTO notifications (user_id, title, message, type, is_read) VALUES
(1, 'Welcome to LibriVault!', 'Welcome to LibriVault, System Administrator! You have full access to manage the library system.', 'WELCOME', FALSE),
(2, 'Welcome to LibriVault!', 'Welcome to LibriVault, John! As a librarian, you can manage book requests and oversee your assigned categories.', 'WELCOME', FALSE),
(3, 'Welcome to LibriVault!', 'Welcome to LibriVault, Sarah! As a librarian, you can manage book requests and oversee your assigned categories.', 'WELCOME', FALSE),
(4, 'Welcome to LibriVault!', 'Welcome to LibriVault, Alice! You can now browse our extensive book collection and start borrowing books.', 'WELCOME', FALSE),
(5, 'Welcome to LibriVault!', 'Welcome to LibriVault, Bob! You can now browse our extensive book collection and start borrowing books.', 'WELCOME', FALSE),
(6, 'Welcome to LibriVault!', 'Welcome to LibriVault, Carol! You can now browse our extensive book collection and start borrowing books.', 'WELCOME', FALSE);

-- Borrow approval notifications
INSERT INTO notifications (user_id, title, message, type, is_read) VALUES
(4, 'Book Borrow Request Approved', 'Your request to borrow "The Great Gatsby" has been approved! You can now access the full book.', 'BORROW_APPROVED', FALSE),
(5, 'Book Borrow Request Approved', 'Your request to borrow "Clean Code" has been approved! You can now access the full book.', 'BORROW_APPROVED', FALSE);

-- Display setup completion message
SELECT 'LibriVault database setup completed successfully!' AS Status;
SELECT 'Default login credentials:' AS Info;
SELECT 'Admin: admin@librivault.com / admin123' AS Admin;
SELECT 'Librarian: librarian1@librivault.com / librarian123' AS Librarian1;
SELECT 'Librarian: librarian2@librivault.com / librarian123' AS Librarian2;
SELECT 'Reader: reader1@librivault.com / reader123' AS Reader1;
SELECT 'Reader: reader2@librivault.com / reader123' AS Reader2;
SELECT 'Reader: reader3@librivault.com / reader123' AS Reader3;