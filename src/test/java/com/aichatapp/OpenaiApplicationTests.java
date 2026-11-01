package com.aichatapp;

import com.aichatapp.controller.ChatController;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {"spring.ai.openai.api-key=${OPENAI_API_KEY:test-key}",
                    "logging.level.org.springframework.ai=DEBUG",
        "spring.docker.compose.skip.in-tests=false"})
class OpenaiApplicationTests {

    @Autowired
    private ChatController chatController;
    @Autowired
    private ChatModel chatModel;
    // Minimum acceptable relevancy score
    @Value("${test.relevancy.min-score:0.7}")
    private float minRelevancyScore;

    @BeforeEach
    void setup(){
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor());
        this.chatClient = chatClientBuilder.build();
        this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
    }
    private ChatClient chatClient;
    private RelevancyEvaluator relevancyEvaluator;
	@Test
    @DisplayName("Should return relevant response for basic geography question")
    @Timeout(value = 30)//30 seconds
	void chatControllerRelevancy() {
        //given
        String question = "What is the capital of India?";
        //when
        String aiResponse = chatController.callAiChat(question);
        EvaluationRequest evalReq = new EvaluationRequest(question,aiResponse);
        EvaluationResponse response = relevancyEvaluator.evaluate(evalReq);

        Assertions.assertAll(() -> assertThat(aiResponse).isNotBlank(),
                () -> assertThat(response.isPass())
                        .withFailMessage("""
                                ========================================
                                The answer was not considered relevant.
                                Question: "%s"
                                Response: "%s"
                                ========================================
                                """, question, aiResponse)
                        .isTrue(),
                () -> assertThat(response.getScore())
                        .withFailMessage("""
                                ========================================
                                The score %.2f is lower than the minimum required %.2f.
                                Question: "%s"
                                Response: "%s"
                                ========================================
                                """, response.getScore(), minRelevancyScore, question, aiResponse)
                        .isGreaterThan(minRelevancyScore));
	}

}
