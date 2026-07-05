package com.morotech.books_rating_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BooksRatingApiApplication {

	public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(BooksRatingApiApplication.class, args);
	}

}
