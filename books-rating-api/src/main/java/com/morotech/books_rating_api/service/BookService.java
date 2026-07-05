package com.morotech.books_rating_api.service;

import com.morotech.books_rating_api.client.BookProviderClient;
import com.morotech.books_rating_api.domain.Review;
import com.morotech.books_rating_api.dto.*;
import com.morotech.books_rating_api.exception.BookNotFoundException;
import com.morotech.books_rating_api.repository.BookRatingAggregate;
import com.morotech.books_rating_api.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookProviderClient gutendexClient;
    private final ReviewRepository reviewRepository;

    public BookService(BookProviderClient gutendexClient, ReviewRepository reviewRepository) {
        this.gutendexClient = gutendexClient;
        this.reviewRepository = reviewRepository;
    }

    public BookSearchResponse search(String title, Integer page) {
        GutendexSearchResponse gutendexResponse = gutendexClient.searchBooks(title, page);
        List<BookSummary> books = gutendexResponse.results().stream().map(this::toSummary).toList();
        return new BookSearchResponse(
                books,
                gutendexResponse.count(),
                page == null ? 1 : page,
                gutendexResponse.next() != null,
                gutendexResponse.previous() != null
                );
    }

    public BookDetailResponse getBookDetails(Long id) {
        GutendexBook book = gutendexClient.getBookById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(id);
        Double avg = reviewRepository.findAverageRatingByBookId(id);

        return new BookDetailResponse(
                book.id(),
                book.title(),
                toAuthorDtos(book),
                book.languages(),
                book.downloadCount(),
                roundToOneDecimal(avg),
                reviews.stream().map(Review::getReviewText).toList()
        );
    }

    public List<TopBookResponse> getTopRatedBooks(int limit) {
        List<BookRatingAggregate> aggregates =
                reviewRepository.findTopRatedBooks(PageRequest.of(0, limit));
        return aggregates.stream()
                .map(a -> new TopBookResponse(
                        a.getBookId(),
                        gutendexClient.getBookById(a.getBookId()).map(GutendexBook::title).orElse(null),
                        roundToOneDecimal(a.getAverageRating()),
                        a.getReviewCount()))
                .toList();
    }

    public List<MonthlyRatingResponse> getMonthlyAverageRatings(Long bookId) {
        gutendexClient.getBookById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        Map<YearMonth, List<Review>> byMonth = reviewRepository
                .findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .collect(Collectors.groupingBy(
                        r -> YearMonth.from(r.getCreatedAt().atZone(ZoneOffset.UTC))));

        return byMonth.entrySet().stream()
                .map(e -> new MonthlyRatingResponse(
                        e.getKey().toString(), // "2026-07"
                        roundToOneDecimal(e.getValue().stream()
                                .mapToInt(Review::getRating).average().orElse(0)),
                        e.getValue().size()))
                .sorted(Comparator.comparing(MonthlyRatingResponse::month))
                .toList();
    }

    private BookSummary toSummary(GutendexBook b) {
        return new BookSummary(b.id(), b.title(), toAuthorDtos(b), b.languages(), b.downloadCount());
    }

    private List<AuthorDto> toAuthorDtos(GutendexBook b) {
        if (b.authors() == null) return List.of();
        return b.authors().stream()
                .map(a -> new AuthorDto(a.name(), a.birthYear(), a.deathYear()))
                .toList();
    }

    private Double roundToOneDecimal(Double value) {
        if (value == null) return null;
        return Math.round(value * 10.0) / 10.0;
    }
}
