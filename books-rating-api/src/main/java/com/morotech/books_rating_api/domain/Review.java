package com.morotech.books_rating_api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reviews", indexes = @Index(name = "idx_reviews_book_id", columnList = "book_id"))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private int rating;

    @Column(name = "review_text", nullable = false, length = 5000)
    private String reviewText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Review() {}

    public Review(Long bookId, int rating, String reviewText) {
        this.bookId = bookId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getBookId() { return bookId; }

    public int getRating() { return rating; }

    public String getReviewText() { return reviewText; }

    public Instant getCreatedAt() { return createdAt; }

}
