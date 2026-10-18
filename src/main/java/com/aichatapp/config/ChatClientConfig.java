package com.aichatapp.config;

import com.aichatapp.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {


    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel){
        return ChatClient.create(openAiChatModel);
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel){
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),new TokenUsageAdvisor()))
                .build();
    }

    @Bean
    public ChatClient ollamaChatClientWithOptions(OllamaChatModel ollamaChatModel){
        /*OllamaChatOptions chatOptions = OllamaChatOptions.builder()
                .temperature(0.7)
                .build();*/
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),new TokenUsageAdvisor()))
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.8))
                .build();
    }


}
