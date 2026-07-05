package com.morotech.books_rating_api.repository;

public interface BookRatingAggregate {
    Long getBookId();
    Double getAverageRating();
    Integer getReviewCount();
}
