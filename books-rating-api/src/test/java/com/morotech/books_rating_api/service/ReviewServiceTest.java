package com.morotech.books_rating_api.service;

import com.morotech.books_rating_api.client.BookProviderClient;
import com.morotech.books_rating_api.domain.Review;
import com.morotech.books_rating_api.dto.CreateReviewRequest;
import com.morotech.books_rating_api.dto.GutendexBook;
import com.morotech.books_rating_api.dto.ReviewResponse;
import com.morotech.books_rating_api.exception.BookNotFoundException;
import com.morotech.books_rating_api.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookProviderClient gutendexClient;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, gutendexClient);
    }

    @Test
    void addReview_savesAndReturnsResponse_whenBookExists() {
        CreateReviewRequest request = new CreateReviewRequest(84L, 5, "Amazing");
        when(gutendexClient.getBookById(84L))
                .thenReturn(Optional.of(new GutendexBook(84L, "T", List.of(), List.of(), 1)));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        ReviewResponse response = reviewService.addReview(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.bookId()).isEqualTo(84L);
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.review()).isEqualTo("Amazing");
        assertThat(response.createdAt()).isNotNull();

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getBookId()).isEqualTo(84L);
        assertThat(captor.getValue().getRating()).isEqualTo(5);
        assertThat(captor.getValue().getReviewText()).isEqualTo("Amazing");
    }

    @Test
    void addReview_throwsBookNotFoundException_andNeverSaves_whenBookMissing() {
        CreateReviewRequest request = new CreateReviewRequest(999L, 3, "Nope");
        when(gutendexClient.getBookById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview(request))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(reviewRepository);
    }
}