package com.aichatapp.config;

import com.aichatapp.advisor.TokenUsageAdvisor;
import com.aichatapp.rag.WebSearchDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class WebSearchRAGChatClientConfig {
    @Bean("webSearchRAGChatClient")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 ChatMemory chatMemory,
                                 RestClient.Builder restClientBuilder){

        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor tokenUsageAdvisor = new TokenUsageAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        RetrievalAugmentationAdvisor webSearchRagAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever( WebSearchDocumentRetriever.builder().
                        restClientBuilder(restClientBuilder).
                        maxResults(5).
                        build()
                ).build();
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(List.of(loggerAdvisor,tokenUsageAdvisor,memoryAdvisor,webSearchRagAdvisor))
                .build();
    }
}
