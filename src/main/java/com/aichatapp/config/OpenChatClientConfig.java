package com.aichatapp.config;

import com.aichatapp.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//for asking any kind of open questions
@Configuration
public class OpenChatClientConfig {
    @Bean("openChatClient")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, SemanticCacheAdvisor semanticCacheAdvisor){
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),
                        new TokenUsageAdvisor(),
                        semanticCacheAdvisor))
                .build();
    }
}
