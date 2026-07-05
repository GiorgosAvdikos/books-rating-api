package com.morotech.books_rating_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateReviewRequest(

        @Schema(description = "Gutendex book id being reviewed", example = "84")
        @NotNull(message = "bookId is required")
        @Positive(message = "bookId must be a positive number")
        Long bookId,

        @Schema(description = "Rating from 0 to 5", example = "4")
        @NotNull(message = "rating is required")
        @Min(value = 0, message = "rating must be between 0 and 5")
        @Max(value = 5, message = "rating must be between 0 and 5")
        Integer rating,

        @Schema(description = "Free-text review",
                example = "It's been fifty years since I had read Frankenstein, and, now—after a "
                        + "recent second reading—I am pleased to know that the pleasures of that first "
                        + "reading have been revived.")
        @NotBlank(message = "review must not be blank")
        @Size(max = 5000, message = "review must be at most 5000 characters")
        String review
) {}
