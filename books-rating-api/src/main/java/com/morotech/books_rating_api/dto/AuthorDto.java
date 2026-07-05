package com.morotech.books_rating_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorDto(
        String name,
        @JsonProperty("birth_year") Integer birthYear,
        @JsonProperty("death_year") Integer deathYear
) {}
