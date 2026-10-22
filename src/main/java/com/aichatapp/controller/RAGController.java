package com.aichatapp.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag-api")
public class RAGController {
    private final ChatClient chatClient;
    private final VectorStore vectoreStore;
    @Value("classpath:/sys-prompt-template/rag-sys-prompt-random-data-example.st")
    Resource randomDataTemplate;
    @Value("classpath:/sys-prompt-template/rag-sys-prompt-hrpolicy.st")
    private Resource hrSysTemplate;
    private final RetrievalAugmentationAdvisor ragAdvisor;
    private final ChatClient webSearchChatClient;

    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient, VectorStore vectoreStore,
                         @Qualifier("retrievalAugmentationAdvisor")RetrievalAugmentationAdvisor ragAdvisor,
                         @Qualifier("webSearchRAGChatClient") ChatClient webSearchChatClient){
        this.chatClient = chatClient;
        this.vectoreStore = vectoreStore;
        this.ragAdvisor = ragAdvisor;
        this.webSearchChatClient = webSearchChatClient;
    }

    //@GetMapping("/random-data/chat") - part of basic understading, now we are not using it
    public ResponseEntity<String> randomDataChat(@RequestHeader("username") String username, @RequestParam("message") String message){
       /* SearchRequest searchReq = SearchRequest.builder().query(message)
                .topK(3)
                .similarityThreshold(0.5)
                //.filterExpression()TODO: need to check for current example
                .build();
        List<Document> similarDocs = vectoreStore.similaritySearch(searchReq);
        String similarContext = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));*/

        String llmResp = chatClient.prompt()
                /*.system(promptSystemSpec -> promptSystemSpec.text(randomDataTemplate)
                        .param("documents",similarContext))*/
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,username))
                .user(message)
                .call()
                .content();
        return ResponseEntity.ok(llmResp);
    }

    @GetMapping("/document/chat")
    public ResponseEntity<String> documentChat(@RequestParam("message") String message, @RequestHeader("username") String username){
        /*SearchRequest searchReq = SearchRequest.builder().query(message).topK(3).similarityThreshold(0.5).build();
        List<Document> similarDocs = vectoreStore.similaritySearch(searchReq);

        String similarContext = similarDocs.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
*/
        String llmResp = chatClient.prompt()
                /*.system(p->p.text(hrSysTemplate)
                        .param("documents",similarContext))*/
                .advisors(ragAdvisor)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,username))
                .user(message)
                .call()
                .content();
        return ResponseEntity.ok(llmResp);

    }

    @GetMapping("/web-search/chat")
    public ResponseEntity<String> webSearchChat(@RequestParam("message") String message, @RequestHeader("username")String username){
        String llmResp = webSearchChatClient.prompt()
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,username))
                .user(message)
                .call()
                .content();
        return ResponseEntity.ok(llmResp);
    }
}
