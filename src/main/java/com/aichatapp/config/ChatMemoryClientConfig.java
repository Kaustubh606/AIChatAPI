package com.aichatapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatMemoryClientConfig {
    @Bean
    public ChatClient chatMemoryChatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory){

        Advisor advisorMemory = MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor advisorLogger = new SimpleLoggerAdvisor();
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(List.of(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()))
                .build();
    }
}
