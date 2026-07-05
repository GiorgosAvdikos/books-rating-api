package com.morotech.books_rating_api.dto;

public record TopBookResponse(Long bookId, String title, double averageRating, long reviewCount) {}
