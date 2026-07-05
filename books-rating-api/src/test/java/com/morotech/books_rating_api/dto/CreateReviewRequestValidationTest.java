package com.morotech.books_rating_api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateReviewRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        CreateReviewRequest request = new CreateReviewRequest(1L, 5, "Great book");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void nullBookId_isRejected() {
        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(null, 3, "ok"));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("bookId");
    }

    @Test
    void nonPositiveBookId_isRejected() {
        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(0L, 3, "ok"));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("bookId");
    }

    @Test
    void nullRating_isRejected() {
        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(1L, null, "ok"));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("rating");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6})
    void ratingOutOfRange_isRejected(int rating) {
        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(1L, rating, "ok"));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("rating");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    void ratingBoundaries_areAccepted(int rating) {
        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(1L, rating, "ok"));

        assertThat(violations).isEmpty();
    }

    @Test
    void blankReview_isRejected() {
        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(1L, 3, "   "));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("review");
    }

    @Test
    void reviewTooLong_isRejected() {
        String longReview = "a".repeat(5001);

        Set<ConstraintViolation<CreateReviewRequest>> violations =
                validator.validate(new CreateReviewRequest(1L, 3, longReview));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("review");
    }
}