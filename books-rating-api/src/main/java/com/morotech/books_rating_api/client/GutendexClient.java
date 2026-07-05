package com.morotech.books_rating_api.client;

import com.morotech.books_rating_api.dto.GutendexBook;
import com.morotech.books_rating_api.dto.GutendexSearchResponse;
import com.morotech.books_rating_api.exception.GutendexException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class GutendexClient implements BookProviderClient {

    private final RestClient restClient;

    public GutendexClient(RestClient.Builder builder,
                          @Value("${gutendex.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @Cacheable(cacheNames = "gutendexSearch", key = "#search + '#' + (#page == null ? 0 : #page)")
    public GutendexSearchResponse searchBooks(String search, Integer page) {
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/books")
                            .queryParam("search", search)
                            .queryParamIfPresent("page", Optional.ofNullable(page))
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        throw new GutendexException(
                                "Gutendex search failed with status " + res.getStatusCode());
                    })
                    .body(GutendexSearchResponse.class);
        } catch (RestClientException ex) { // network/timeout/parse
            throw new GutendexException("Could not reach Gutendex while searching", ex);
        }
    }

    @Override
    @Cacheable(cacheNames = "gutendexBook", key = "#id")
    public Optional<GutendexBook> getBookById(Long id) {
        try {
            return restClient.get()
                    .uri("/books/{id}", id)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().value() == 404) {
                            return Optional.empty();
                        }
                        if (response.getStatusCode().isError()) {
                            throw new GutendexException(
                                    "Gutendex returned " + response.getStatusCode() + " for book " + id);
                        }
                        return Optional.ofNullable(response.bodyTo(GutendexBook.class));
                    });
        } catch (RestClientException ex) {
            throw new GutendexException("Could not reach Gutendex while fetching book with id = " + id, ex);
        }
    }

}
