-- Insert a test member with a username
INSERT INTO members (username, email, name, password, role, active, membership_date, borrowing_limit, total_fines)
VALUES (
    'testuser',
    'test@library.com',
    'Test User',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',   -- password = 'admin123' (BCrypt)
    'MEMBER',
    true,
    CURDATE(),
    5,
    0.0
);