package com.morotech.books_rating_api.dto;

public record MonthlyRatingResponse(String month, double averageRating, long reviewCount) {}
