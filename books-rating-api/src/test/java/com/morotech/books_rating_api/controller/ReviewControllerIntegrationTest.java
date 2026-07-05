package com.morotech.books_rating_api.controller;

import com.morotech.books_rating_api.client.BookProviderClient;
import com.morotech.books_rating_api.dto.CreateReviewRequest;
import com.morotech.books_rating_api.dto.GutendexBook;
import com.morotech.books_rating_api.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookProviderClient bookProviderClient;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void createReview_returns201_andPersists_whenBookExists() throws Exception {
        when(bookProviderClient.getBookById(84L))
                .thenReturn(Optional.of(new GutendexBook(84L, "T", List.of(), List.of(), 1)));
        CreateReviewRequest request = new CreateReviewRequest(84L, 5, "Amazing read");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(84))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.review").value("Amazing read"))
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(reviewRepository.findByBookIdOrderByCreatedAtDesc(84L)).hasSize(1);
    }

    @Test
    void createReview_returns404_whenBookNotFound() throws Exception {
        when(bookProviderClient.getBookById(999L)).thenReturn(Optional.empty());
        CreateReviewRequest request = new CreateReviewRequest(999L, 3, "Nope");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertThat(reviewRepository.count()).isZero();
    }

    @Test
    void createReview_returns400_whenRatingOutOfRange() throws Exception {
        String body = """
                {"bookId":1,"rating":9,"review":"text"}
                """;

        mockMvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void createReview_returns400_whenReviewBlank() throws Exception {
        String body = """
                {"bookId":1,"rating":3,"review":"   "}
                """;

        mockMvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.review").exists());
    }

    @Test
    void createReview_returns400_whenBookIdMissing() throws Exception {
        String body = """
                {"rating":3,"review":"text"}
                """;

        mockMvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bookId").exists());
    }
}