package com.morotech.books_rating_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GutendexSearchResponse(
        Integer count,
        String next,
        String previous,
        List<GutendexBook> results
) {}
