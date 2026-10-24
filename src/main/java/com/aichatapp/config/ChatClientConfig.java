package com.aichatapp.config;

import com.aichatapp.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient openAiChatClient(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),new TokenUsageAdvisor()))
                .build();
    }

    @Bean
    public ChatClient openAiChatClientWithOptions(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),new TokenUsageAdvisor()))
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-5.4-mini"))
                .build();
    }


}
