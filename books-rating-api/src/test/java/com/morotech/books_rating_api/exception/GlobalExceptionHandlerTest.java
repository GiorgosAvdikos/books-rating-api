package com.morotech.books_rating_api.exception;

import com.morotech.books_rating_api.controller.ReviewController;
import com.morotech.books_rating_api.dto.CreateReviewRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404WithDetailMessage() {
        ProblemDetail pd = handler.handleNotFound(new BookNotFoundException(84L));

        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getTitle()).isEqualTo("Book not found");
        assertThat(pd.getDetail()).contains("84");
    }

    @Test
    void handleBodyValidation_returns400WithFieldErrors() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(
                new CreateReviewRequest(null, null, ""), "createReviewRequest");
        bindingResult.addError(new FieldError("createReviewRequest", "bookId", "bookId is required"));
        bindingResult.addError(new FieldError("createReviewRequest", "rating", "rating is required"));

        MethodParameter methodParameter = new MethodParameter(
                ReviewController.class.getMethod("createReview", CreateReviewRequest.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ProblemDetail pd = handler.handleBodyValidation(ex);

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid request");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) pd.getProperties().get("errors");
        assertThat(errors)
                .containsEntry("bookId", "bookId is required")
                .containsEntry("rating", "rating is required");
    }

    @Test
    void handleParamValidation_returns400WithMessage() {
        ConstraintViolationException ex = new ConstraintViolationException("page must be positive", Set.of());

        ProblemDetail pd = handler.handleParamValidation(ex);

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid request parameter");
        assertThat(pd.getDetail()).isEqualTo("page must be positive");
    }

    @Test
    void handleGutendex_returns502WithGenericMessage() {
        ProblemDetail pd = handler.handleGutendex(new GutendexException("upstream boom"));

        assertThat(pd.getStatus()).isEqualTo(502);
        assertThat(pd.getTitle()).isEqualTo("Upstream service error");
        assertThat(pd.getDetail()).contains("temporarily unavailable");
    }

    @Test
    void handleMissingRequestParameter_returns400WithParameterName() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("search", "String");

        ProblemDetail pd = handler.handleMissingRequestParameter(ex);

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid request parameter");
        assertThat(pd.getDetail()).isEqualTo("Missing required parameter: search");
    }

    @Test
    void handleUnexpected_returns500WithGenericMessage() {
        ProblemDetail pd = handler.handleUnexpected(new RuntimeException("New exception"));

        assertThat(pd.getStatus()).isEqualTo(500);
        assertThat(pd.getTitle()).isEqualTo("Internal server error");
        assertThat(pd.getDetail()).contains("unexpected error");
    }
}