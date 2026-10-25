package com.aichatapp.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tool")
public class TimeController {
    private final ChatClient timeChatClient;

    public TimeController(@Qualifier("timeChatClient") ChatClient timeChatClient){
        this.timeChatClient = timeChatClient;
    }

    @GetMapping("local-time")
    public ResponseEntity<String> getLocalTimeForLLM(@RequestHeader("username") String username, @RequestParam("message") String message){
        String llmResp = timeChatClient.prompt()
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,username))
                .user(message)
                .call()
                .content();
        return ResponseEntity.ok(llmResp);
    }

}
