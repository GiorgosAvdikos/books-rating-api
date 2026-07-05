package com.morotech.books_rating_api.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Book with id " + id + " was not found");
    }
}
