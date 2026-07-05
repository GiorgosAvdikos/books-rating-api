package com.morotech.books_rating_api.controller;

import com.morotech.books_rating_api.dto.*;
import com.morotech.books_rating_api.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Validated
@Tag(name = "Books", description = "Search books, view details with reviews, and rating stats")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Search books by title (Part 1)",
            description = "Proxies the Gutendex search API. Supports optional pagination via 'page'.")
    public BookSearchResponse search(
            @Parameter(description = "Title to search for", example = "frankenstein")
            @RequestParam("search") @NotBlank String search,
            @Parameter(description = "1-based page number (optional)", example = "1")
            @RequestParam(value = "page", required = false) @Positive Integer page) {
        return bookService.search(search, page);
    }

    @GetMapping("/top")
    @Operation(summary = "Top rated books (bonus)",
            description = "Returns the top N books by average rating, then by number of reviews.")
    public List<TopBookResponse> topBooks(
            @Parameter(description = "Maximum number of books to return", example = "10")
            @RequestParam(defaultValue = "10") @Positive int limit) {
        return bookService.getTopRatedBooks(limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book details, average rating and reviews (Part 3)",
            description = "Combines Gutendex book details with the average rating and reviews stored locally.")
    public BookDetailResponse getBook(
            @Parameter(description = "Gutendex book id", example = "84") @PathVariable Long id) {
        return bookService.getBookDetails(id);
    }

    @GetMapping("/{id}/ratings/monthly")
    @Operation(summary = "Average rating per month for a book (bonus)")
    public List<MonthlyRatingResponse> monthlyRatings(
            @Parameter(description = "Gutendex book id", example = "84") @PathVariable Long id) {
        return bookService.getMonthlyAverageRatings(id);
    }
}
