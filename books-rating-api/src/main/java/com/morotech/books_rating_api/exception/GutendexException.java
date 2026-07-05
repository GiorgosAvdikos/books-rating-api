package com.morotech.books_rating_api.exception;

public class GutendexException extends RuntimeException {

    public GutendexException(String message) {
        super(message);
    }

    public GutendexException(String message, Throwable cause) {
        super(message, cause);
    }
}
