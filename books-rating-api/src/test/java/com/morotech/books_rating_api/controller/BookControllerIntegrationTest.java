package com.morotech.books_rating_api.controller;

import com.morotech.books_rating_api.client.BookProviderClient;
import com.morotech.books_rating_api.domain.Review;
import com.morotech.books_rating_api.dto.GutendexAuthor;
import com.morotech.books_rating_api.dto.GutendexBook;
import com.morotech.books_rating_api.dto.GutendexSearchResponse;
import com.morotech.books_rating_api.exception.GutendexException;
import com.morotech.books_rating_api.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookProviderClient bookProviderClient;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void search_returnsBooks_forValidQuery() throws Exception {
        GutendexBook book = new GutendexBook(84L, "Frankenstein",
                List.of(new GutendexAuthor("Mary Shelley", 1797, 1851)), List.of("en"), 1000);
        GutendexSearchResponse response = new GutendexSearchResponse(1, null, null, List.of(book));
        when(bookProviderClient.searchBooks("frankenstein", null)).thenReturn(response);

        mockMvc.perform(get("/api/books").param("search", "frankenstein"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].title").value("Frankenstein"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void search_returns400_whenSearchParamMissing() throws Exception {

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_returns400_whenPageNotPositive() throws Exception {
        mockMvc.perform(get("/api/books").param("search", "x").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBook_returnsDetails_whenBookExists() throws Exception {
        GutendexBook book = new GutendexBook(84L, "Frankenstein", List.of(), List.of("en"), 1000);
        when(bookProviderClient.getBookById(84L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/books/84"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(84))
                .andExpect(jsonPath("$.title").value("Frankenstein"))
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews").isEmpty());
    }

    @Test
    void getBook_returns404_whenBookNotFound() throws Exception {
        when(bookProviderClient.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book not found"));
    }

    @Test
    void getBook_returns502_whenUpstreamFails() throws Exception {
        when(bookProviderClient.getBookById(1L)).thenThrow(new GutendexException("New exception"));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void topBooks_returnsAggregatedTopRatedBooks() throws Exception {
        reviewRepository.save(new Review(1L, 5, "great"));
        when(bookProviderClient.getBookById(1L))
                .thenReturn(Optional.of(new GutendexBook(1L, "Book1", List.of(), List.of(), 1)));

        mockMvc.perform(get("/api/books/top").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(1))
                .andExpect(jsonPath("$[0].title").value("Book1"))
                .andExpect(jsonPath("$[0].averageRating").value(5.0));
    }

    @Test
    void monthlyRatings_returnsGroupedAverages() throws Exception {
        when(bookProviderClient.getBookById(1L))
                .thenReturn(Optional.of(new GutendexBook(1L, "Book1", List.of(), List.of(), 1)));
        reviewRepository.save(new Review(1L, 4, "r1"));


        mockMvc.perform(get("/api/books/1/ratings/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewCount").value(1));
    }

    @Test
    void monthlyRatings_returns404_whenBookNotFound() throws Exception {
        when(bookProviderClient.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999/ratings/monthly"))
                .andExpect(status().isNotFound());
    }
}