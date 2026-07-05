package com.morotech.books_rating_api.client;

import com.morotech.books_rating_api.dto.GutendexBook;
import com.morotech.books_rating_api.dto.GutendexSearchResponse;

import java.util.Optional;

public interface BookProviderClient {

    GutendexSearchResponse searchBooks(String search, Integer page);

    Optional<GutendexBook> getBookById(Long id);
}
