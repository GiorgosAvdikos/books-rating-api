package com.morotech.books_rating_api.client;

import com.morotech.books_rating_api.dto.GutendexSearchResponse;
import com.morotech.books_rating_api.exception.GutendexException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(GutendexClient.class)
class GutendexClientTest {

    @Autowired
    private GutendexClient gutendexClient;

    @Autowired
    private MockRestServiceServer server;

    @BeforeEach
    void resetServer() {
        server.reset();
    }

    @Test
    void searchBooks_parsesResponseBody() {
        server.expect(requestTo(containsString("/books?search=frankenstein")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count":1,"next":null,"previous":null,"results":[
                          {"id":84,"title":"Frankenstein","authors":[{"name":"Mary Shelley","birth_year":1797,"death_year":1851}],"languages":["en"],"download_count":1000}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        GutendexSearchResponse response = gutendexClient.searchBooks("frankenstein", null);

        assertThat(response.count()).isEqualTo(1);
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).title()).isEqualTo("Frankenstein");
        assertThat(response.results().get(0).authors().get(0).name()).isEqualTo("Mary Shelley");
    }

    @Test
    void searchBooks_includesPageParam_whenProvided() {
        server.expect(requestTo(containsString("page=2")))
                .andRespond(withSuccess("""
                        {"count":0,"next":null,"previous":null,"results":[]}
                        """, MediaType.APPLICATION_JSON));

        gutendexClient.searchBooks("x", 2);
    }

    @Test
    void searchBooks_throwsGutendexException_on5xxResponse() {
        server.expect(requestTo(containsString("/books")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> gutendexClient.searchBooks("x", null))
                .isInstanceOf(GutendexException.class);
    }

    @Test
    void searchBooks_throwsGutendexException_onConnectionFailure() {
        server.expect(requestTo(containsString("/books")))
                .andRespond(request -> {
                    throw new IOException("connection reset");
                });

        assertThatThrownBy(() -> gutendexClient.searchBooks("x", null))
                .isInstanceOf(GutendexException.class);
    }

    @Test
    void getBookById_returnsBook_whenFound() {
        server.expect(requestTo(endsWith("/books/84")))
                .andRespond(withSuccess("""
                        {"id":84,"title":"Frankenstein","authors":[],"languages":["en"],"download_count":1000}
                        """, MediaType.APPLICATION_JSON));

        Optional<com.morotech.books_rating_api.dto.GutendexBook> result = gutendexClient.getBookById(84L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Frankenstein");
    }

    @Test
    void getBookById_returnsEmpty_on404() {
        server.expect(requestTo(endsWith("/books/999")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<com.morotech.books_rating_api.dto.GutendexBook> result = gutendexClient.getBookById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getBookById_throwsGutendexException_on5xx() {
        server.expect(requestTo(endsWith("/books/1")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> gutendexClient.getBookById(1L))
                .isInstanceOf(GutendexException.class);
    }

    @Test
    void getBookById_throwsGutendexException_onConnectionFailure() {
        server.expect(requestTo(endsWith("/books/1")))
                .andRespond(request -> {
                    throw new IOException("connection reset");
                });

        assertThatThrownBy(() -> gutendexClient.getBookById(1L))
                .isInstanceOf(GutendexException.class);
    }
}