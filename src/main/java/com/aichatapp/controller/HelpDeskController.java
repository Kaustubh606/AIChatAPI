package com.aichatapp.controller;

import com.aichatapp.tools.HelpDeskTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tool")
public class HelpDeskController {
    private final HelpDeskTools helpDeskTools;
    private final ChatClient helpDeskChatClient;

    public HelpDeskController(@Qualifier("helpDeskChatClient")ChatClient helpDeskChatClient,HelpDeskTools helpDeskTools){
        this.helpDeskChatClient = helpDeskChatClient;
        this.helpDeskTools = helpDeskTools;
    }

    @GetMapping("/helpdesk-chat")
    public ResponseEntity<String> helpDeskChat(@RequestParam("message") String message, @RequestHeader("username") String username){
        String response = helpDeskChatClient.prompt()
                .tools(helpDeskTools)
                .toolContext(Map.of("username",username))
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,username))
                .user(message)
                .call()
                .content();
        return ResponseEntity.ok(response);
    }
}
