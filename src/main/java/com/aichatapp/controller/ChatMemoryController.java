package com.aichatapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatMemoryController {
    private static final Logger log = LoggerFactory.getLogger(ChatMemoryController.class);
    private final ChatClient chatMemoryClient;

    public ChatMemoryController(@Qualifier("chatMemoryChatClient")ChatClient chatMemoryClient ){
        this.chatMemoryClient = chatMemoryClient;
    }

    @GetMapping("/gpt/chat-memory")
    public ResponseEntity<String> chatWithMemory(@RequestParam("message") String message) {
        return ResponseEntity.ok(chatMemoryClient.prompt()
                .user(message)
                        .advisors(advisorSpec ->advisorSpec.param(ChatMemory.CONVERSATION_ID,"default"))
                .call()
                .content());
    }
}
