package com.morotech.books_rating_api.service;

import com.morotech.books_rating_api.client.BookProviderClient;
import com.morotech.books_rating_api.domain.Review;
import com.morotech.books_rating_api.dto.CreateReviewRequest;
import com.morotech.books_rating_api.dto.ReviewResponse;
import com.morotech.books_rating_api.exception.BookNotFoundException;
import com.morotech.books_rating_api.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookProviderClient gutendexClient;

    public ReviewService(ReviewRepository reviewRepository, BookProviderClient gutendexClient) {
        this.reviewRepository = reviewRepository;
        this.gutendexClient = gutendexClient;
    }

    @Transactional
    public ReviewResponse addReview(CreateReviewRequest request) {
        gutendexClient.getBookById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Review saved = reviewRepository.save(new Review(request.bookId(), request.rating(), request.review()));

        return new ReviewResponse(saved.getId(), saved.getBookId(), saved.getRating(),
                saved.getReviewText(), saved.getCreatedAt());
    }
}
