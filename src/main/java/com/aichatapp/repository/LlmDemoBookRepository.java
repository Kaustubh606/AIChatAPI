package com.aichatapp.repository;

import com.aichatapp.entity.LlmDemoBook;
import com.aichatapp.model.BookRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Standard JPA repository. Every method here is BLOCKING: it waits on JDBC.
 * BookStreamService is responsible for running these calls off the reactive threads.
 *
 * The record queries use a JPQL constructor expression ("SELECT new ..."), so
 * Hibernate builds BookRecord objects directly from the result set without
 * creating managed entities first.
 */
public interface LlmDemoBookRepository extends JpaRepository<LlmDemoBook, Long> {

    /**
     * Shared SELECT clause. It is a compile-time constant (string literals joined
     * with +), which is why it can be used inside the @Query annotations below.
     */
    String SELECT_RECORD =
            "SELECT new com.aichatapp.model.BookRecord("
          + " b.bookId, b.isbn, b.title, b.author, b.genre, b.publisher,"
          + " b.publishedDate, b.pageCount, b.priceInr, b.rating,"
          + " b.bookFormat, b.inStock, b.description)"
          + " FROM LlmDemoBook b ";

    @Query(SELECT_RECORD + "ORDER BY b.bookId")
    List<BookRecord> findAllAsRecords();

    @Query(SELECT_RECORD + "WHERE b.genre = :genre ORDER BY b.title")
    List<BookRecord> findByGenreAsRecords(@Param("genre") String genre);

    /**
     * One page at a time. Returning List (not Page) means Spring Data skips the
     * extra COUNT(*) query. On Oracle 12c+ Hibernate uses OFFSET ... FETCH NEXT.
     */
    @Query(SELECT_RECORD + "ORDER BY b.bookId")
    List<BookRecord> findPageAsRecords(Pageable pageable);

    Optional<LlmDemoBook> findByIsbn(String isbn);
}
