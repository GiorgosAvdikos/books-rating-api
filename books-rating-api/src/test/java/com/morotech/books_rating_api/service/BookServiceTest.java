package com.morotech.books_rating_api.service;

import com.morotech.books_rating_api.client.BookProviderClient;
import com.morotech.books_rating_api.domain.Review;
import com.morotech.books_rating_api.dto.*;
import com.morotech.books_rating_api.exception.BookNotFoundException;
import com.morotech.books_rating_api.repository.BookRatingAggregate;
import com.morotech.books_rating_api.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookProviderClient gutendexClient;

    @Mock
    private ReviewRepository reviewRepository;

    private BookService bookService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        bookService = new BookService(gutendexClient, reviewRepository);
    }

    @Test
    void search_mapsBooksAndPagingFlags() {
        GutendexAuthor author = new GutendexAuthor("Mary Shelley", 1797, 1851);
        GutendexBook book = new GutendexBook(84L, "Frankenstein", List.of(author), List.of("en"), 1000);
        GutendexSearchResponse response = new GutendexSearchResponse(1, "next-url", null, List.of(book));
        when(gutendexClient.searchBooks("frankenstein", 2)).thenReturn(response);

        BookSearchResponse result = bookService.search("frankenstein", 2);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.books()).hasSize(1);

        BookSummary summary = result.books().get(0);
        assertThat(summary.id()).isEqualTo(84L);
        assertThat(summary.title()).isEqualTo("Frankenstein");
        assertThat(summary.authors()).containsExactly(new AuthorDto("Mary Shelley", 1797, 1851));
    }

    @Test
    void search_defaultsPageToOne_whenPageIsNull() {
        GutendexSearchResponse response = new GutendexSearchResponse(0, null, null, List.of());
        when(gutendexClient.searchBooks("x", null)).thenReturn(response);

        BookSearchResponse result = bookService.search("x", null);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    void search_treatsNullAuthorsAsEmptyList() {
        GutendexBook book = new GutendexBook(1L, "Title", null, List.of("en"), 5);
        GutendexSearchResponse response = new GutendexSearchResponse(1, null, null, List.of(book));
        when(gutendexClient.searchBooks("t", null)).thenReturn(response);

        BookSearchResponse result = bookService.search("t", null);

        assertThat(result.books().get(0).authors()).isEmpty();
    }

    @Test
    void getBookDetails_returnsDetailsWithRoundedAverageAndReviews() {
        GutendexBook book = new GutendexBook(84L, "Frankenstein", List.of(), List.of("en"), 1000);
        when(gutendexClient.getBookById(84L)).thenReturn(Optional.of(book));
        Review review1 = new Review(84L, 5, "Great");
        Review review2 = new Review(84L, 4, "Good");
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(84L)).thenReturn(List.of(review1, review2));
        when(reviewRepository.findAverageRatingByBookId(84L)).thenReturn(4.55);

        BookDetailResponse result = bookService.getBookDetails(84L);

        assertThat(result.id()).isEqualTo(84L);
        assertThat(result.title()).isEqualTo("Frankenstein");
        assertThat(result.rating()).isEqualTo(4.6);
        assertThat(result.reviews()).containsExactly("Great", "Good");
    }

    @Test
    void getBookDetails_returnsNullRating_whenNoReviews() {
        GutendexBook book = new GutendexBook(84L, "Title", List.of(), List.of(), 0);
        when(gutendexClient.getBookById(84L)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(84L)).thenReturn(List.of());
        when(reviewRepository.findAverageRatingByBookId(84L)).thenReturn(null);

        BookDetailResponse result = bookService.getBookDetails(84L);

        assertThat(result.rating()).isNull();
        assertThat(result.reviews()).isEmpty();
    }

    @Test
    void getBookDetails_throwsBookNotFoundException_whenMissingUpstream() {
        when(gutendexClient.getBookById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookDetails(999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void getTopRatedBooks_mapsAggregatesWithTitleFromProvider() {
        BookRatingAggregate aggregate = mock(BookRatingAggregate.class);
        when(aggregate.getBookId()).thenReturn(1L);
        when(aggregate.getAverageRating()).thenReturn(4.666);
        when(aggregate.getReviewCount()).thenReturn(3);
        when(reviewRepository.findTopRatedBooks(PageRequest.of(0, 5))).thenReturn(List.of(aggregate));
        when(gutendexClient.getBookById(1L))
                .thenReturn(Optional.of(new GutendexBook(1L, "Title1", List.of(), List.of(), 10)));

        List<TopBookResponse> result = bookService.getTopRatedBooks(5);

        assertThat(result).hasSize(1);
        TopBookResponse r = result.get(0);
        assertThat(r.bookId()).isEqualTo(1L);
        assertThat(r.title()).isEqualTo("Title1");
        assertThat(r.averageRating()).isEqualTo(4.7);
        assertThat(r.reviewCount()).isEqualTo(3L);
    }

    @Test
    void getTopRatedBooks_usesNullTitle_whenBookMissingFromProvider() {
        BookRatingAggregate aggregate = mock(BookRatingAggregate.class);
        when(aggregate.getBookId()).thenReturn(2L);
        when(aggregate.getAverageRating()).thenReturn(3.0);
        when(aggregate.getReviewCount()).thenReturn(1);
        when(reviewRepository.findTopRatedBooks(any())).thenReturn(List.of(aggregate));
        when(gutendexClient.getBookById(2L)).thenReturn(Optional.empty());

        List<TopBookResponse> result = bookService.getTopRatedBooks(5);

        assertThat(result.get(0).title()).isNull();
    }

    @Test
    void getMonthlyAverageRatings_groupsByCalendarMonthAndSortsAscending() {
        when(gutendexClient.getBookById(84L))
                .thenReturn(Optional.of(new GutendexBook(84L, "T", List.of(), List.of(), 1)));

        Review januaryReview = new Review(84L, 4, "jan");
        ReflectionTestUtils.setField(januaryReview, "createdAt", Instant.parse("2026-01-15T00:00:00Z"));
        Review februaryReview1 = new Review(84L, 2, "feb1");
        ReflectionTestUtils.setField(februaryReview1, "createdAt", Instant.parse("2026-02-10T00:00:00Z"));
        Review februaryReview2 = new Review(84L, 4, "feb2");
        ReflectionTestUtils.setField(februaryReview2, "createdAt", Instant.parse("2026-02-20T00:00:00Z"));

        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(84L))
                .thenReturn(List.of(februaryReview2, februaryReview1, januaryReview));

        List<MonthlyRatingResponse> result = bookService.getMonthlyAverageRatings(84L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).month()).isEqualTo("2026-01");
        assertThat(result.get(0).averageRating()).isEqualTo(4.0);
        assertThat(result.get(0).reviewCount()).isEqualTo(1);
        assertThat(result.get(1).month()).isEqualTo("2026-02");
        assertThat(result.get(1).averageRating()).isEqualTo(3.0);
        assertThat(result.get(1).reviewCount()).isEqualTo(2);
    }

    @Test
    void getMonthlyAverageRatings_throwsBookNotFoundException_whenBookMissing() {
        when(gutendexClient.getBookById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getMonthlyAverageRatings(999L))
                .isInstanceOf(BookNotFoundException.class);

        verifyNoInteractions(reviewRepository);
    }
}