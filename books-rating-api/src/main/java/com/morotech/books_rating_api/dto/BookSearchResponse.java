package com.morotech.books_rating_api.dto;

import java.util.List;

public record BookSearchResponse(
        List<BookSummary> books,
        Integer count,
        Integer page,
        boolean hasNext,
        boolean hasPrevious
) {}
