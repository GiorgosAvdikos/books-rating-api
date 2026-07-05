package com.morotech.books_rating_api.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long bookId,
        int rating,
        String review,
        Instant createdAt
) {}
