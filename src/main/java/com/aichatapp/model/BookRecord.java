package com.aichatapp.model;

import com.aichatapp.entity.BookFormat;
import com.aichatapp.entity.LlmDemoBook;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable view of one book. This is what the controller streams as Flux<BookRecord>.
 *
 * Why a record and not the entity:
 *  - Immutable, so it is safe to pass between Reactor threads.
 *  - Not attached to a persistence context, so no LazyInitializationException
 *    and no accidental dirty-checking.
 *  - The API shape stays stable even if the table changes.
 *
 * The component order and types must match the JPQL "SELECT new ..." expression
 * in LlmDemoBookRepository exactly.
 */
public record BookRecord(
        Long id,
        String isbn,
        String title,
        String author,
        String genre,
        String publisher,
        LocalDate publishedDate,
        Integer pageCount,
        BigDecimal priceInr,
        BigDecimal rating,
        BookFormat format,
        Boolean inStock,
        String description
) {

    /** For cases where you already have an entity, e.g. findById. */
    public static BookRecord from(LlmDemoBook b) {
        return new BookRecord(
                b.getBookId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getGenre(),
                b.getPublisher(), b.getPublishedDate(), b.getPageCount(), b.getPriceInr(),
                b.getRating(), b.getBookFormat(), b.getInStock(), b.getDescription());
    }
}
