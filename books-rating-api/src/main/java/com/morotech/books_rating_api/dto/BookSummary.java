package com.morotech.books_rating_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BookSummary(
        Long id,
        String title,
        List<AuthorDto> authors,
        List<String> languages,
        @JsonProperty("download_count") Integer downloadCount
) {}
