CREATE TABLE IF NOT EXISTS reviews (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     BIGINT        NOT NULL,
    rating      INTEGER       NOT NULL,
    review_text VARCHAR(5000) NOT NULL,
    created_at  TIMESTAMP     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews (book_id);