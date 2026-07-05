package com.morotech.books_rating_api.controller;

import com.morotech.books_rating_api.dto.CreateReviewRequest;
import com.morotech.books_rating_api.dto.ReviewResponse;
import com.morotech.books_rating_api.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Part 2 — rate and review a book")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Post a review and rating for a book",
            description = "Rating must be between 0 and 5. The book id must exist in Gutendex, "
                    + "otherwise the review is rejected to keep the database clean.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload (bad rating, blank review, ...)"),
            @ApiResponse(responseCode = "404", description = "Book id not found in Gutendex"),
            @ApiResponse(responseCode = "502", description = "Gutendex is unavailable")
    })
    public ReviewResponse createReview(@Valid @RequestBody CreateReviewRequest request) {
        return reviewService.addReview(request);
    }
}
