package com.morotech.books_rating_api.repository;

import com.morotech.books_rating_api.domain.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

    @Query("select avg(r.rating) from Review r where r.bookId = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    @Query("""
           select r.bookId as bookId,
                  avg(r.rating) as averageRating,
                  count(r) as reviewCount
           from Review r
           group by r.bookId
           order by avg(r.rating) desc, count(r) desc
           """)
    List<BookRatingAggregate> findTopRatedBooks(Pageable pageable);
}
