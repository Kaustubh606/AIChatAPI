package com.aichatapp.config;

import com.aichatapp.rag.PIIMaskingDocPostProcessor;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class ChatMemoryClientConfig {
    @Bean
    public ChatClient chatMemoryChatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory,
                                           SemanticCacheAdvisor semanticCacheAdvisor){

        Advisor advisorMemory = MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor advisorLogger = new SimpleLoggerAdvisor();
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(List.of(
                        advisorLogger,
                        advisorMemory,
                        semanticCacheAdvisor))
                .build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            VectorStore vectorStore,
            @Qualifier("openAiChatModel") ChatModel chatModel,
            @Value("classpath:/sys-prompt-template/rag-sys-prompt-hrpolicy.st") Resource hrTemplate) {

        QueryTransformer translator = TranslationQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel)
                        .defaultOptions(ChatOptions.builder().temperature(0.0)))
                .targetLanguage("english")
                .build();

        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.5)
                .build();

        ContextualQueryAugmenter augmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(new PromptTemplate(hrTemplate))
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(translator)
                .documentRetriever(retriever)
                .documentPostProcessors(new PIIMaskingDocPostProcessor())
                .queryAugmenter(augmenter)
                .build();
    }
}
