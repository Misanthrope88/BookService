INSERT INTO categories (id, name, description, is_deleted) VALUES
    (1, 'Fiction', 'Fiction books', false),
    (2, 'Classics', 'Classic literature', false),
    (3, 'Archived', 'Deleted category', true);

INSERT INTO books (id, title, author, isbn, price, is_deleted) VALUES
    (1, 'The Hobbit', 'J. R. R. Tolkien', '9780000000001', 20.00, false),
    (2, 'The Lord of the Rings', 'J. R. R. Tolkien', '9780000000002', 30.00, false),
    (3, 'Pride and Prejudice', 'Jane Austen', '9780000000003', 15.00, false),
    (4, 'The Silmarillion', 'J. R. R. Tolkien', '9780000000004', 25.00, true);

INSERT INTO books_categories (book_id, category_id) VALUES
    (1, 1), (1, 2), (1, 3), (2, 1), (3, 2), (4, 1);
