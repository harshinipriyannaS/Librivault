-- LibriVault Initial Data
-- This file inserts initial data into the database

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Insert default admin user (password: admin123)
INSERT IGNORE INTO users (id, email, password, first_name, last_name, role, reader_credits, active) VALUES
(1, 'admin@librivault.com', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System', 'Administrator', 'ADMIN', 0, TRUE);

-- Insert sample librarian users (password: librarian123)
INSERT IGNORE INTO users (id, email, password, first_name, last_name, role, reader_credits, active) VALUES
(2, 'librarian1@librivault.com', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John', 'Smith', 'LIBRARIAN', 0, TRUE),
(3, 'librarian2@librivault.com', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah', 'Johnson', 'LIBRARIAN', 0, TRUE);

-- Insert sample reader users (password: reader123)
INSERT IGNORE INTO users (id, email, password, first_name, last_name, role, reader_credits, active) VALUES
(4, 'harshavardhaman1305@gmail.com', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Alice', 'Brown', 'READER', 2, TRUE),
(5, 'reader2@librivault.com', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Bob', 'Wilson', 'READER', 0, TRUE),
(6, 'reader3@librivault.com', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Carol', 'Davis', 'READER', 1, TRUE);

-- Insert librarian records
INSERT IGNORE INTO librarians (id, user_id, employee_id, department, hire_date, active) VALUES
( 2, 'LIB001', 'Fiction', '2023-01-15', TRUE),
( 3, 'LIB002', 'Science & Technology', '2023-02-20', TRUE);

-- Insert book categories
INSERT IGNORE INTO categories (id, name, description, assigned_librarian_id, active) VALUES
( 'Fiction', 'Novels, short stories, and other fictional works', 1, TRUE),
( 'Non-Fiction', 'Biographies, memoirs, and factual books', 1, TRUE),
( 'Science & Technology', 'Computer science, engineering, and scientific research', 2, TRUE),
( 'Business & Economics', 'Business strategy, economics, and finance', 2, TRUE),
( 'History', 'Historical events, periods, and figures', 1, TRUE),
( 'Self-Help', 'Personal development and improvement books', 1, TRUE),
( 'Education', 'Academic textbooks and educational materials', 2, TRUE),
('Arts & Culture', 'Art, music, literature, and cultural studies', 1, TRUE);

-- Insert sample books (with actual S3 URIs - using your uploaded book as example)
INSERT IGNORE INTO books 
(title, author, isbn, description, category_id, s3_uri, cover_image_uri, total_copies, available_copies, published_date, active) 
VALUES
( 'A Tale of Two Cities', 'Charles Dickens', '9780486406510', 'A historical novel set in London and Paris before and during the French Revolution.', 2, 's3://librivault-book-storage/A tale of two cities-Charles Dickens.pdf', 'https://th.bing.com/th/id/OIP.J1vjevZJQ2Z2CFrjpu2J9AHaJP?w=202&h=253&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1859-11-26', TRUE),
('Blockchain For Dummies', 'Tiana Laurence', '9781119365594', 'A beginner-friendly introduction to blockchain technology and its applications.', 7, 's3://librivault-book-storage/Blockchain For Dummies.pdf', 'https://m.media-amazon.com/images/I/71j7wF0xE2L.jpg', 3, 3, '2017-05-01', TRUE),
( 'Dangerous Liaisons', 'Pierre Choderlos de Laclos', '9780140449570', 'A French epistolary novel exploring manipulation and seduction among the aristocracy.', 1, 's3://librivault-book-storage/Dangerous-Liaisons-Pierre-Choderlos-de-Laclos.pdf', 'https://th.bing.com/th/id/OIP.Evk_Tpu4Re-ueQ6Eg56zAgHaL2?w=197&h=316&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1782-03-23', TRUE),
( 'Foundations of Machine Learning (2nd Edition)', 'Mehryar Mohri', '9780262039406', 'A comprehensive and mathematically rigorous introduction to machine learning.', 7, 's3://librivault-book-storage/Foundations of Machine- Second Edition.pdf', 'https://cdn2.penguin.com.au/covers/original/9780262039406.jpg', 3, 3, '2018-12-04', TRUE),
( 'Introduction to Ecology', 'Worku Legess, Teklu Mulugeta, Aragaw Ambelu', '9789994450725', 'An introductory textbook covering ecological principles and environmental interactions.', 3, 's3://librivault-book-storage/Introduction to Ecology Author Worku Legess, Teklu Mulugeta, Aragaw Ambelu.pdf', 'https://th.bing.com/th/id/OIP.4IT7K1FMhOQndUidENyHHgAAAA?w=202&h=261&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2014-01-01', TRUE),
('Introduction To Physical Oceanography', 'Robert H. Stewart', '9780972903367', 'An essential guide to oceanographic processes, currents, and climate influences.', 3, 's3://librivault-book-storage/Introduction To Physical Oceanography Author Robert H. Stewart.pdf', 'https://th.bing.com/th/id/OIP.tt_cbeXsstAJV--kGBF6TQHaJl?w=202&h=262&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2008-09-10', TRUE),
( 'Manual of Standard Managements in Obstetrics and Gynaecology', 'IdMicro', '9789980770120', 'A medical reference guide for doctors and nurses in obstetric and gynecologic care.', 3, 's3://librivault-book-storage/Manual of Standard Managements in Obstetrics and Gynaecology for Doctors H E O s and Nurses in Papua New Guinea Author IdMicro.pdf', 'https://pictures.abebooks.com/inventory/10167130076.jpg', 3, 3, '2016-05-01', TRUE),
( 'Master Your Mind', 'Anonymous', '9788179927852', 'A motivational guide to controlling thoughts and improving mental focus.', 6, 's3://librivault-book-storage/Master-your-mind-anonymous.pdf', 'https://th.bing.com/th/id/OIP.0l-4l4mj2UHKzBg49ays9AAAAA?w=202&h=202&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2009-04-01', TRUE),
( 'Poor Folk', 'Fyodor Dostoyevsky', '9780140447262', 'A poignant epistolary novel exploring poverty and human dignity.', 1, 's3://librivault-book-storage/Poor folk-Fyodor Dostoyevsky.pdf', 'https://th.bing.com/th/id/OIP.wxAKHeMlyC_o_bMu4Y9C5wAAAA?w=202&h=303&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1846-01-01', TRUE),
( 'The Complete Guide To Organizational Design', 'Navalent', '9781119528500', 'A complete framework for designing effective organizational structures.', 4, 's3://librivault-book-storage/The Complete Guide To Organizational Design Author Navalent.pdf', 'https://th.bing.com/th/id/OIP.4XIWo9dJiR03ld-JNqeivgAAAA?w=202&h=286&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2018-01-10', TRUE),
( 'The Iron Heel', 'Jack London', '9780140182927', 'A dystopian novel exploring class struggle and social revolution.', 2, 's3://librivault-book-storage/The Iron Heel - Jack London.pdf', 'https://th.bing.com/th/id/OIP.B0fdBa9Ti8tXTBQYzzUGNgHaLV?w=202&h=309&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1908-03-01', TRUE),
( 'The Secret of Success', 'William Walker Atkinson', '9780879801875', 'A motivational book emphasizing the mental foundations of success.', 6, 's3://librivault-book-storage/The Secret of Success, William Walker Atkinson.pdf', 'https://th.bing.com/th/id/OIP.ugsnXSJsqmXHM6lQA0-4ogHaKe?w=202&h=286&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1908-06-01', TRUE),
( 'The Time Machine', 'H. G. Wells', '9780451528551', 'A science fiction classic about time travel and social commentary.', 1, 's3://librivault-book-storage/The Time Machine - H G Wells.pdf', 'https://img.perlego.com/book-covers/1409948/9781451658866_300_450.webp', 3, 3, '1895-05-07', TRUE),
( 'Yoga', 'Obooko Mind', '9788172235014', 'A guide to yoga philosophy and practice for mental and physical well-being.', 6, 's3://librivault-book-storage/Yoga-safronof-obooko-mind0034.pdf', 'https://th.bing.com/th/id/OIP.i1cU5dv0yBRN-ioYPe8VkgHaLS?w=119&h=182&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2010-01-01', TRUE);


-- Insert default subscriptions for READER users only (not for admin/librarians)
-- User 1: Admin (no subscription)
-- User 2: Librarian (no subscription) 
-- User 3: Librarian (no subscription)
-- User 4-6: Readers (have subscriptions)
INSERT IGNORE INTO subscriptions (user_id, type, start_date, end_date, book_limit, duration_days, daily_fine_amount, price, active) VALUES
(4, 'FREE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 2, 14, 2.00, 0.00, TRUE),
(5, 'PREMIUM', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY), -1, 30, 1.00, 9.99, TRUE),
(6, 'FREE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 2, 14, 2.00, 0.00, TRUE);

-- Insert sample borrow requests
INSERT IGNORE INTO borrow_requests (id, reader_id, book_id, librarian_id, status, request_message, response_message, requested_at, responded_at) VALUES
( 4, 1, 2, 'APPROVED', 'I would like to read this classic novel.', 'Request approved. Enjoy reading!', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
( 5, 3, 3, 'APPROVED', 'Need this for my software development project.', 'Approved for your project work.', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
( 6, 4, 2, 'PENDING', 'Interested in learning about startup methodologies.', NULL, NOW(), NULL);

-- Insert sample borrow records
INSERT IGNORE INTO borrow_records (id, reader_id, book_id, borrowed_at, due_date, returned_at, status, used_credit, credits_earned) VALUES
( 4, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 29 DAY), NULL, 'ACTIVE', FALSE, 0),
( 5, 4, NOW(), DATE_ADD(NOW(), INTERVAL 299 DAY), NULL, 'ACTIVE', FALSE, 0);

-- Insert welcome notifications for all users
INSERT IGNORE INTO notifications (user_id, title, message, type, is_read) VALUES
(1, 'Welcome to LibriVault!', 'Welcome to LibriVault, System Administrator! You have full access to manage the library system.', 'WELCOME', FALSE),
(2, 'Welcome to LibriVault!', 'Welcome to LibriVault, John! As a librarian, you can manage book requests and oversee your assigned categories.', 'WELCOME', FALSE),
(3, 'Welcome to LibriVault!', 'Welcome to LibriVault, Sarah! As a librarian, you can manage book requests and oversee your assigned categories.', 'WELCOME', FALSE),
(4, 'Welcome to LibriVault!', 'Welcome to LibriVault, Alice! You can now browse our extensive book collection and start borrowing books.', 'WELCOME', FALSE),
(5, 'Welcome to LibriVault!', 'Welcome to LibriVault, Bob! You can now browse our extensive book collection and start borrowing books.', 'WELCOME', FALSE),
(6, 'Welcome to LibriVault!', 'Welcome to LibriVault, Carol! You can now browse our extensive book collection and start borrowing books.', 'WELCOME', FALSE);

-- Insert sample borrow approval notifications
INSERT IGNORE INTO notifications (user_id, title, message, type, is_read) VALUES
(4, 'Book Borrow Request Approved', 'Your request to borrow "A Tale of Two Cities" has been approved! You can now access the full book.', 'BORROW_REQUEST_APPROVED', FALSE),
(5, 'Book Borrow Request Approved', 'Your request to borrow "Clean Code" has been approved! You can now access the full book.', 'BORROW_REQUEST_APPROVED', FALSE);

-- Update book availability after borrows
UPDATE books SET available_copies = available_copies - 1 WHERE id IN (1, 4);

-- Reset AUTO_INCREMENT values to ensure proper sequencing
ALTER TABLE users AUTO_INCREMENT = 7;
ALTER TABLE librarians AUTO_INCREMENT = 3;
ALTER TABLE categories AUTO_INCREMENT = 9;
ALTER TABLE books AUTO_INCREMENT = 15;
ALTER TABLE subscriptions AUTO_INCREMENT = 4;
ALTER TABLE borrow_requests AUTO_INCREMENT = 4;
ALTER TABLE borrow_records AUTO_INCREMENT = 3;
ALTER TABLE notifications AUTO_INCREMENT = 9;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;