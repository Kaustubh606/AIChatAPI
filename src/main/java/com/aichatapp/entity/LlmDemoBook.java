package com.aichatapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for the LLM_DEMO_BOOK table.
 *
 * This has to be a class, not a record: JPA needs a no-arg constructor and
 * non-final fields so it can create the object and fill it in. Records have
 * neither. The record (BookRecord) is what leaves the service layer.
 */
@Entity
@Table(name = "LLM_DEMO_BOOK")
public class LlmDemoBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Oracle identity column (12c+)
    @Column(name = "BOOK_ID")
    private Long bookId;

    @Column(name = "ISBN", nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "AUTHOR", nullable = false, length = 100)
    private String author;

    @Column(name = "GENRE", nullable = false, length = 50)
    private String genre;

    @Column(name = "PUBLISHER", nullable = false, length = 100)
    private String publisher;

    @Column(name = "PUBLISHED_DATE", nullable = false)
    private LocalDate publishedDate;

    @Column(name = "PAGE_COUNT", nullable = false)
    private Integer pageCount;

    @Column(name = "PRICE_INR", nullable = false, precision = 8, scale = 2)
    private BigDecimal priceInr;

    @Column(name = "RATING", precision = 2, scale = 1)
    private BigDecimal rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "BOOK_FORMAT", nullable = false, length = 10)
    private BookFormat bookFormat;

    @Convert(converter = YesNoBooleanConverter.class)
    @Column(name = "IN_STOCK", nullable = false, length = 1)
    private Boolean inStock;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    /** Filled by the column default (SYSTIMESTAMP); JPA never writes it. */
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LlmDemoBook() {
        // required by JPA
    }

    public Long getBookId()            { return bookId; }
    public String getIsbn()            { return isbn; }
    public String getTitle()           { return title; }
    public String getAuthor()          { return author; }
    public String getGenre()           { return genre; }
    public String getPublisher()       { return publisher; }
    public LocalDate getPublishedDate(){ return publishedDate; }
    public Integer getPageCount()      { return pageCount; }
    public BigDecimal getPriceInr()    { return priceInr; }
    public BigDecimal getRating()      { return rating; }
    public BookFormat getBookFormat()  { return bookFormat; }
    public Boolean getInStock()        { return inStock; }
    public String getDescription()     { return description; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
