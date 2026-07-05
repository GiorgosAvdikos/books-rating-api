package com.morotech.books_rating_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    private final String swaggerPath;

    public OpenApiConfig(@Value("${app.swagger-path:/swagger-ui.html}") String swaggerPath) {
        this.swaggerPath = swaggerPath;
    }

    @Bean
    public OpenAPI bookRatingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Book Rating API")
                .description("""
                        Search Project Gutenberg books (via the Gutendex API), post ratings and
                        reviews, and fetch a book's details together with its average rating and reviews.

                        Use the "Try it out" button on any endpoint below to call the API directly.""")
                .version("1.0.0")
                .contact(new Contact().name("Moro Technology — Software Engineer Challenge")));
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", swaggerPath);
    }
}