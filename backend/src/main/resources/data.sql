-- LibriVault Initial Data
-- This file inserts initial data into the database

-- Insert default admin user (password: admin123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, reader_credits, active) VALUES
('admin@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System', 'Administrator', 'ADMIN', 0, TRUE);

-- Insert sample librarian users (password: librarian123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, reader_credits, active) VALUES
('librarian1@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John', 'Smith', 'LIBRARIAN', 0, TRUE),
('librarian2@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah', 'Johnson', 'LIBRARIAN', 0, TRUE);

-- Insert sample reader users (password: reader123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, reader_credits, active) VALUES
('harshavardhaman1305@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Alice', 'Brown', 'READER', 2, TRUE),
('reader2@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Bob', 'Wilson', 'READER', 0, TRUE),
('reader3@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Carol', 'Davis', 'READER', 1, TRUE);



-- Basic categories for testing
INSERT IGNORE INTO categories (name, description, active) VALUES
('Fiction', 'Novels and fictional works', TRUE),
('Non-Fiction', 'Educational and factual books', TRUE),
('Science', 'Scientific and technical books', TRUE);