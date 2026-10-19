package com.aichatapp.service;

import com.aichatapp.model.BookRecord;
import com.aichatapp.repository.LlmDemoBookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Supplier;

/**
 * Bridges blocking JPA calls into Reactor.
 *
 * Wrapping a JPA call in Flux does NOT make the database call non-blocking.
 * The JDBC thread still waits for Oracle. What we can do is run that wait on
 * Schedulers.boundedElastic(), a pool meant for blocking work, so it never
 * ties up the small set of event-loop threads that serve every request.
 *
 * For true end-to-end non-blocking access, use R2DBC with the Oracle R2DBC
 * driver instead of JPA.
 */
@Service
public class BookStreamService {

    private static final Logger log = LoggerFactory.getLogger(BookStreamService.class);

    private final LlmDemoBookRepository repository;

    public BookStreamService(LlmDemoBookRepository repository) {
        this.repository = repository;
    }

    /** All 500 books. One query, then the list is emitted item by item. */
    public Flux<BookRecord> findAll() {
        return fromBlockingList(repository::findAllAsRecords);
    }

    public Flux<BookRecord> findByGenre(String genre) {
        return fromBlockingList(() -> repository.findByGenreAsRecords(genre));
    }

    /**
     * Fetches one page, emits its books, and only then fetches the next page.
     * The database is read at the speed the subscriber consumes (backpressure).
     * If the client disconnects, the Flux is cancelled and no further pages are fetched.
     *
     * Watch the "Fetching page" log lines while streaming to see this happen.
     */
    public Flux<BookRecord> streamInPages(int pageSize) {
        return Flux.range(0, Integer.MAX_VALUE)
                .concatMap(page -> Mono.fromCallable(() -> {
                                    log.info("Fetching page {} (size {}) on {}",
                                            page, pageSize, Thread.currentThread().getName());
                                    return repository.findPageAsRecords(PageRequest.of(page, pageSize));
                                })
                                .subscribeOn(Schedulers.boundedElastic()))
                .takeWhile(books -> !books.isEmpty())
                // prefetch 1: ask for the next page only after the current one is used up
                .flatMapIterable(books -> books, 1);
    }

    /**
     * Mono.fromCallable defers the query until someone subscribes.
     * Calling repository.findAllAsRecords() directly and passing the result to
     * Flux.fromIterable would run the query immediately, on the caller's thread,
     * even if nobody ever subscribes.
     */
    private static Flux<BookRecord> fromBlockingList(Supplier<List<BookRecord>> query) {
        return Mono.fromCallable(query::get)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }
}
