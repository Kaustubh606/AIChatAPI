package com.aichatapp.controller;

import com.aichatapp.model.BookRecord;
import com.aichatapp.model.CountryCities;
import com.aichatapp.service.BookStreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;


@RestController
@RequestMapping("/api")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient openAiChatClient;
    private final ChatClient openAiChatClientWithOptions;
    @Autowired
    private BookStreamService bookService;

    @Value("classpath:/prompt-stuffing/hr-policy-sys-prompt-template.st")
    Resource hrPoliciesSysPrompt;
        /*public ChatController(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }*/
    @Value("classpath:/prompt-stuffing/resume-analysis-sys-prompt.st")
    Resource resumeAnalysisSysPrompt;

    public ChatController(@Qualifier("openAiChatClient")ChatClient openAIChatClient,
                          @Qualifier("openAiChatClientWithOptions")ChatClient openAiChatClientWithOptions){
        this.openAiChatClient = openAIChatClient;
        this.openAiChatClientWithOptions = openAiChatClientWithOptions;
    }


    @GetMapping("/openai/chat")
    public String callAiChat(@RequestParam("message") String message) {
        return openAiChatClient.prompt(message).call().content();
    }

    @GetMapping("/openai/hr-chat")
    public String callHrChat(@RequestParam("message") String message) {
        return openAiChatClient.prompt()
                .system(hrPoliciesSysPrompt)
                .user(message)
                //.advisors(new SimpleLoggerAdvisor())
                //.advisors(new TokenUsageAdvisor())
                .call()
                .content();
    }

    @GetMapping("/openai/resume-chat")
    public String callResumeAnalysisChat(@RequestParam("message") String message) {
        return openAiChatClient.prompt()
                .system(resumeAnalysisSysPrompt)
                .user(message)
                //.advisors(new SimpleLoggerAdvisor())
                //.advisors(new TokenUsageAdvisor())
                .call()
                .content();
    }

    @GetMapping("/openai/resume-chat-v2")
    public String callResumeAnalysisChatWithOptions(@RequestParam("message") String message) {
        return openAiChatClientWithOptions.prompt()
                .system(resumeAnalysisSysPrompt)
                .user(message)
                //.advisors(new SimpleLoggerAdvisor())
                //.advisors(new TokenUsageAdvisor())
                .call()
                .content();
    }

    @GetMapping("/openai/resume-stream-resp")
    public Flux<String> callStreamResponse(@RequestParam("message") String message) {
        return openAiChatClient.prompt()
                .system(resumeAnalysisSysPrompt)
                .user(message)
                .stream()
                .content();
        //aata hach response DB madhey takun bagh kay hotay RDBMS madhey TODO:
    }

    /*BOOKS CONTROLLER ====================*/
    /**
     * GET /api/books
     * GET /api/books?genre=Mystery
     *
     * Returns a normal JSON array. The client sees nothing until the whole
     * response is written, so this looks just like a List endpoint.
     */
    @GetMapping(value = "/books",produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BookRecord> findBooks(@RequestParam(required = false) String genre) {
        return (genre == null || genre.isBlank())
                ? bookService.findAll()
                : bookService.findByGenre(genre);
    }

    /**
     * GET /api/books/books-stream
     * GET /api/books/books-stream?pageSize=20&delayMs=500
     *
     * Server-Sent Events: each book is sent the moment it is emitted, so in a
     * browser you see them arrive one by one. This is the endpoint to demo Flux.
     */
    @GetMapping(value = "/books-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BookRecord> streamBooks(@RequestParam(defaultValue = "50") int pageSize,
                                        @RequestParam(defaultValue = "100") long delayMs) {

        int safePageSize = Math.max(1, Math.min(pageSize, 200));
        long safeDelay = Math.max(0, Math.min(delayMs, 5000));

        Flux<BookRecord> books = bookService.streamInPages(safePageSize);
        if (safeDelay > 0) {
            books = books.delayElements(Duration.ofMillis(safeDelay));
        }
        return books
                .doOnSubscribe(s -> log.info("Stream started"))
                .doOnComplete(() -> log.info("Stream completed"))
                .doOnCancel(() -> log.info("Client disconnected, stream cancelled"));
    }

    @GetMapping("/openai/structured-resp")
    public ResponseEntity<CountryCities> getCountryCityList(@RequestParam("message") String message){
        CountryCities countryCities = openAiChatClient
                .prompt()
                .user(message)
                .call()
                .entity(CountryCities.class);
        return ResponseEntity.ok(countryCities);

    }

}
