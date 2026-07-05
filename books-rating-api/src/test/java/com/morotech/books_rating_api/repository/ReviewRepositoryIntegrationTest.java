package com.morotech.books_rating_api.repository;

import com.morotech.books_rating_api.domain.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ReviewRepositoryIntegrationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void save_assignsGeneratedId() {
        Review saved = reviewRepository.save(new Review(1L, 4, "text"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByBookIdOrderByCreatedAtDesc_returnsNewestFirst() {
        Review older = reviewFor(1L, 4, "older", Instant.now().minus(2, ChronoUnit.HOURS));
        Review newer = reviewFor(1L, 5, "newer", Instant.now().minus(1, ChronoUnit.HOURS));
        reviewRepository.saveAll(List.of(older, newer));

        List<Review> result = reviewRepository.findByBookIdOrderByCreatedAtDesc(1L);

        assertThat(result).extracting(Review::getReviewText).containsExactly("newer", "older");
    }

    @Test
    void findByBookIdOrderByCreatedAtDesc_returnsOnlyMatchingBook() {
        reviewRepository.save(new Review(1L, 4, "for book 1"));
        reviewRepository.save(new Review(2L, 5, "for book 2"));

        List<Review> result = reviewRepository.findByBookIdOrderByCreatedAtDesc(1L);

        assertThat(result).extracting(Review::getReviewText).containsExactly("for book 1");
    }

    @Test
    void findByBookIdOrderByCreatedAtDesc_returnsEmptyList_whenNoReviews() {
        List<Review> result = reviewRepository.findByBookIdOrderByCreatedAtDesc(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAverageRatingByBookId_returnsAverage() {
        reviewRepository.save(new Review(2L, 3, "a"));
        reviewRepository.save(new Review(2L, 5, "b"));

        Double avg = reviewRepository.findAverageRatingByBookId(2L);

        assertThat(avg).isEqualTo(4.0);
    }

    @Test
    void findAverageRatingByBookId_returnsNull_whenNoReviews() {
        Double avg = reviewRepository.findAverageRatingByBookId(999L);

        assertThat(avg).isNull();
    }

    @Test
    void findTopRatedBooks_ordersByAverageDescThenCountDesc_andRespectsLimit() {
        reviewRepository.save(new Review(10L, 5, "x"));
        reviewRepository.save(new Review(20L, 4, "x"));
        reviewRepository.save(new Review(20L, 4, "y"));
        reviewRepository.save(new Review(30L, 3, "x"));

        List<BookRatingAggregate> top = reviewRepository.findTopRatedBooks(PageRequest.of(0, 2));

        assertThat(top).hasSize(2);
        assertThat(top.get(0).getBookId()).isEqualTo(10L);
        assertThat(top.get(0).getAverageRating()).isEqualTo(5.0);
        assertThat(top.get(0).getReviewCount()).isEqualTo(1);
        assertThat(top.get(1).getBookId()).isEqualTo(20L);
        assertThat(top.get(1).getAverageRating()).isEqualTo(4.0);
        assertThat(top.get(1).getReviewCount()).isEqualTo(2);
    }

    @Test
    void findTopRatedBooks_breaksAverageTiesByReviewCountDesc() {
        reviewRepository.save(new Review(40L, 5, "single"));
        reviewRepository.save(new Review(50L, 5, "a"));
        reviewRepository.save(new Review(50L, 5, "b"));

        List<BookRatingAggregate> top = reviewRepository.findTopRatedBooks(PageRequest.of(0, 10));

        assertThat(top).extracting(BookRatingAggregate::getBookId).containsExactly(50L, 40L);
    }

    private Review reviewFor(long bookId, int rating, String text, Instant createdAt) {
        Review review = new Review(bookId, rating, text);
        ReflectionTestUtils.setField(review, "createdAt", createdAt);
        return review;
    }
}