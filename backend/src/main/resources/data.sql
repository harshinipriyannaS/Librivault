-- LibriVault Initial Data
-- This file contains the essential data for the application

-- Insert Categories
INSERT IGNORE INTO categories (name, description, active) VALUES
('Fiction', 'Novels, short stories, and other fictional works', TRUE),
('Non-Fiction', 'Biographies, memoirs, and factual books', TRUE),
('Science & Technology', 'Scientific research, technology, and innovation', TRUE),
('Computer Science', 'Programming, algorithms, and computer science topics', TRUE),
('Biology & Life Sciences', 'Biology, ecology, and life science topics', TRUE),
('Physics & Earth Sciences', 'Physics, oceanography, and earth sciences', TRUE),
('Mathematics', 'Mathematical concepts and applications', TRUE),
('Engineering', 'Engineering principles and applications', TRUE),
('Business & Management', 'Business strategy, management, and organizational design', TRUE),
('Economics', 'Economic theory and applications', TRUE),
('Psychology', 'Psychological studies and mental health', TRUE),
('Philosophy', 'Philosophical thoughts and theories', TRUE),
('Self-Help & Personal Development', 'Personal development and improvement books', TRUE),
('Management & Leadership', 'Leadership and organizational management', TRUE),
('Health & Medicine', 'Medical knowledge and health-related topics', TRUE);

-- Insert Sample Users
-- Admin user (password: admin123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, reader_credits, active) VALUES
('admin@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System', 'Administrator', 'ADMIN', 0, TRUE);

-- Librarian users (password: librarian123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, reader_credits, active) VALUES
('librarian1@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John', 'Smith', 'LIBRARIAN', 0, TRUE),
('librarian2@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah', 'Johnson', 'LIBRARIAN', 0, TRUE);

-- Reader users (password: reader123)
INSERT IGNORE INTO users (email, password, first_name, last_name, role, reader_credits, active) VALUES
('harshavardhaman1305@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Harsha', 'Vardhaman', 'READER', 2, TRUE),
('reader1@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Alice', 'Brown', 'READER', 1, TRUE),
('reader2@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Bob', 'Wilson', 'READER', 0, TRUE),
('reader3@librivault.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Carol', 'Davis', 'READER', 3, TRUE);

-- Insert Your Actual Books (with auto-generated IDs)
INSERT IGNORE INTO books (title, author, isbn, description, category_id, s3_uri, cover_image_uri, total_copies, available_copies, published_date, active) VALUES
('A Tale of Two Cities', 'Charles Dickens', '9780486406510', 'A historical novel set in London and Paris before and during the French Revolution.', 1, 's3://librivault-book-storage/A tale of two cities-Charles Dickens.pdf', 'https://th.bing.com/th/id/OIP.J1vjevZJQ2Z2CFrjpu2J9AHaJP?w=202&h=253&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1859-11-26', TRUE),
('Blockchain For Dummies', 'Tiana Laurence', '9781119365594', 'A beginner-friendly introduction to blockchain technology and its applications.', 3, 's3://librivault-book-storage/Blockchain For Dummies.pdf', 'https://m.media-amazon.com/images/I/71j7wF0xE2L.jpg', 3, 3, '2017-05-01', TRUE),
('Dangerous Liaisons', 'Pierre Choderlos de Laclos', '9780140449570', 'A French epistolary novel exploring manipulation and seduction among the aristocracy.', 1, 's3://librivault-book-storage/Dangerous-Liaisons-Pierre-Choderlos-de-Laclos.pdf', 'https://th.bing.com/th/id/OIP.Evk_Tpu4Re-ueQ6Eg56zAgHaL2?w=197&h=316&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1782-03-23', TRUE),
('Foundations of Machine Learning (2nd Edition)', 'Mehryar Mohri', '9780262039406', 'A comprehensive and mathematically rigorous introduction to machine learning.', 4, 's3://librivault-book-storage/Foundations of Machine- Second Edition.pdf', 'https://cdn2.penguin.com.au/covers/original/9780262039406.jpg', 3, 3, '2018-12-04', TRUE),
('Introduction to Ecology', 'Worku Legess, Teklu Mulugeta, Aragaw Ambelu', '9789994450725', 'An introductory textbook covering ecological principles and environmental interactions.', 5, 's3://librivault-book-storage/Introduction to Ecology Author Worku Legess, Teklu Mulugeta, Aragaw Ambelu.pdf', 'https://th.bing.com/th/id/OIP.4IT7K1FMhOQndUidENyHHgAAAA?w=202&h=261&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2014-01-01', TRUE),
('Introduction To Physical Oceanography', 'Robert H. Stewart', '9780972903367', 'An essential guide to oceanographic processes, currents, and climate influences.', 6, 's3://librivault-book-storage/Introduction To Physical Oceanography Author Robert H. Stewart.pdf', 'https://th.bing.com/th/id/OIP.tt_cbeXsstAJV--kGBF6TQHaJl?w=202&h=262&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2008-09-10', TRUE),
('Manual of Standard Managements in Obstetrics and Gynaecology', 'IdMicro', '9789980770120', 'A medical reference guide for doctors and nurses in obstetric and gynecologic care.', 15, 's3://librivault-book-storage/Manual of Standard Managements in Obstetrics and Gynaecology for Doctors H E O s and Nurses in Papua New Guinea Author IdMicro.pdf', 'https://pictures.abebooks.com/inventory/10167130076.jpg', 3, 3, '2016-05-01', TRUE),
('Master Your Mind', 'Anonymous', '9788179927852', 'A motivational guide to controlling thoughts and improving mental focus.', 13, 's3://librivault-book-storage/Master-your-mind-anonymous.pdf', 'https://th.bing.com/th/id/OIP.0l-4l4mj2UHKzBg49ays9AAAAA?w=202&h=202&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2009-04-01', TRUE),
('Poor Folk', 'Fyodor Dostoyevsky', '9780140447262', 'A poignant epistolary novel exploring poverty and human dignity.', 1, 's3://librivault-book-storage/Poor folk-Fyodor Dostoyevsky.pdf', 'https://th.bing.com/th/id/OIP.wxAKHeMlyC_o_bMu4Y9C5wAAAA?w=202&h=303&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1846-01-01', TRUE),
('The Complete Guide To Organizational Design', 'Navalent', '9781119528500', 'A complete framework for designing effective organizational structures.', 14, 's3://librivault-book-storage/The Complete Guide To Organizational Design Author Navalent.pdf', 'https://th.bing.com/th/id/OIP.4XIWo9dJiR03ld-JNqeivgAAAA?w=202&h=286&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2018-01-10', TRUE),
('The Iron Heel', 'Jack London', '9780140182927', 'A dystopian novel exploring class struggle and social revolution.', 1, 's3://librivault-book-storage/The Iron Heel - Jack London.pdf', 'https://th.bing.com/th/id/OIP.B0fdBa9Ti8tXTBQYzzUGNgHaLV?w=202&h=309&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1908-03-01', TRUE),
('The Secret of Success', 'William Walker Atkinson', '9780879801875', 'A motivational book emphasizing the mental foundations of success.', 13, 's3://librivault-book-storage/The Secret of Success, William Walker Atkinson.pdf', 'https://th.bing.com/th/id/OIP.ugsnXSJsqmXHM6lQA0-4ogHaKe?w=202&h=286&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '1908-06-01', TRUE),
('The Time Machine', 'H. G. Wells', '9780451528551', 'A science fiction classic about time travel and social commentary.', 1, 's3://librivault-book-storage/The Time Machine - H G Wells.pdf', 'https://img.perlego.com/book-covers/1409948/9781451658866_300_450.webp', 3, 3, '1895-05-07', TRUE),
('Yoga', 'Obooko Mind', '9788172235014', 'A guide to yoga philosophy and practice for mental and physical well-being.', 15, 's3://librivault-book-storage/Yoga-safronof-obooko-mind0034.pdf', 'https://th.bing.com/th/id/OIP.i1cU5dv0yBRN-ioYPe8VkgHaLS?w=119&h=182&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3', 3, 3, '2010-01-01', TRUE);

-- Insert Sample Subscriptions for Users (matching Subscription entity)
INSERT IGNORE INTO subscriptions (user_id, type, start_date, end_date, book_limit, borrow_duration_days, daily_fine_amount, active) VALUES
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), 'FREE', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), 2, 30, 10.00, TRUE),
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), 'PREMIUM', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 335 DAY), 10, 30, 5.00, TRUE),
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), 'FREE', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), 2, 30, 10.00, TRUE),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), 'PREMIUM', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 305 DAY), 10, 30, 5.00, TRUE);

-- Insert Sample Payments (matching Payment entity)
INSERT IGNORE INTO payments (user_id, stripe_payment_intent_id, amount, type, status, description) VALUES
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), 'pi_1234567890abcdef', 599.00, 'SUBSCRIPTION', 'COMPLETED', 'Premium subscription payment'),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), 'pi_0987654321fedcba', 599.00, 'SUBSCRIPTION', 'COMPLETED', 'Premium subscription payment');

-- Insert Sample Borrow Records (Active Borrows) - matching BorrowRecord entity
INSERT IGNORE INTO borrow_records (reader_id, book_id, borrowed_at, due_date, status, used_credit, credits_earned) VALUES
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), (SELECT id FROM books WHERE title = 'A Tale of Two Cities'), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 9 DAY), 'ACTIVE', FALSE, 0),
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), (SELECT id FROM books WHERE title = 'The Time Machine'), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 11 DAY), 'ACTIVE', FALSE, 0),
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), (SELECT id FROM books WHERE title = 'Blockchain For Dummies'), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 'ACTIVE', FALSE, 0),
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), (SELECT id FROM books WHERE title = 'Foundations of Machine Learning (2nd Edition)'), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 23 DAY), 'ACTIVE', FALSE, 0),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), (SELECT id FROM books WHERE title = 'Introduction to Ecology'), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 28 DAY), 'ACTIVE', FALSE, 0);

-- Insert Sample Returned Books
INSERT IGNORE INTO borrow_records (reader_id, book_id, borrowed_at, due_date, returned_at, status, used_credit, credits_earned) VALUES
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), (SELECT id FROM books WHERE title = 'Dangerous Liaisons'), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 'RETURNED', FALSE, 1),
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), (SELECT id FROM books WHERE title = 'Poor Folk'), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 'OVERDUE', FALSE, 0),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), (SELECT id FROM books WHERE title = 'The Iron Heel'), DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 'RETURNED', FALSE, 1);

-- Insert Sample Borrow Requests (matching BorrowRequest entity)
INSERT IGNORE INTO borrow_requests (reader_id, book_id, status, requested_at, review_notes) VALUES
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), (SELECT id FROM books WHERE title = 'Introduction To Physical Oceanography'), 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY), 'I need this book for my research project.'),
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), (SELECT id FROM books WHERE title = 'Master Your Mind'), 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Interested in personal development.'),
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), (SELECT id FROM books WHERE title = 'The Complete Guide To Organizational Design'), 'PENDING', NOW(), 'Required for my MBA course.');

-- Insert Sample Fines (matching Fine entity)
INSERT IGNORE INTO fines (reader_id, borrow_record_id, amount, overdue_days, status, description) VALUES
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), (SELECT id FROM borrow_records WHERE reader_id = (SELECT id FROM users WHERE email = 'reader2@librivault.com') AND book_id = (SELECT id FROM books WHERE title = 'Poor Folk')), 20.00, 2, 'PENDING', 'Fine for overdue book: Poor Folk (2 days overdue)');

-- Insert Sample Notifications (matching Notification entity)
INSERT IGNORE INTO notifications (user_id, title, message, type, is_read, reference_id, reference_type) VALUES
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), 'Book Due Soon', 'Your book "A Tale of Two Cities" is due in 2 days.', 'BOOK_DUE_REMINDER', FALSE, (SELECT id FROM books WHERE title = 'A Tale of Two Cities'), 'BOOK'),
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), 'Book Due Soon', 'Your book "The Time Machine" is due in 4 days.', 'BOOK_DUE_REMINDER', FALSE, (SELECT id FROM books WHERE title = 'The Time Machine'), 'BOOK'),
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), 'Welcome to LibriVault!', 'Thank you for joining LibriVault. Enjoy your premium subscription!', 'WELCOME', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), 'Fine Notice', 'You have an unpaid fine of ₹20.00 for late return of "Poor Folk".', 'FINE_GENERATED', FALSE, (SELECT id FROM fines WHERE reader_id = (SELECT id FROM users WHERE email = 'reader2@librivault.com')), 'FINE'),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), 'Book Available', 'The book you requested is now available for borrowing.', 'BORROW_REQUEST_APPROVED', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email = 'reader1@librivault.com'), 'Payment Successful', 'Your premium subscription payment of ₹599.00 has been processed successfully.', 'PAYMENT_SUCCESS', TRUE, (SELECT id FROM payments WHERE user_id = (SELECT id FROM users WHERE email = 'reader1@librivault.com')), 'PAYMENT'),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), 'Payment Successful', 'Your premium subscription payment of ₹599.00 has been processed successfully.', 'PAYMENT_SUCCESS', TRUE, (SELECT id FROM payments WHERE user_id = (SELECT id FROM users WHERE email = 'reader3@librivault.com')), 'PAYMENT'),
((SELECT id FROM users WHERE email = 'harshavardhaman1305@gmail.com'), 'Welcome to LibriVault!', 'Welcome to LibriVault! Start exploring our vast collection of books.', 'WELCOME', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email = 'reader2@librivault.com'), 'Welcome to LibriVault!', 'Welcome to LibriVault! Start exploring our vast collection of books.', 'WELCOME', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email = 'reader3@librivault.com'), 'Welcome to LibriVault!', 'Welcome to LibriVault! Start exploring our vast collection of books.', 'WELCOME', TRUE, NULL, NULL);

-- Update available copies based on active borrows
UPDATE books SET available_copies = total_copies - (
    SELECT COUNT(*) FROM borrow_records 
    WHERE book_id = books.id AND status = 'ACTIVE'
) WHERE id IN (
    SELECT DISTINCT book_id FROM borrow_records WHERE status = 'ACTIVE'
);