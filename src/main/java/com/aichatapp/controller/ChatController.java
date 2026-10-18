package com.aichatapp.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;
    private final ChatClient ollamaChatClientWithOptions;

    @Value("classpath:/prompt-stuffing/hr-policy-sys-prompt-template.st")
    Resource hrPoliciesSysPrompt;
        /*public ChatController(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }*/
    @Value("classpath:/prompt-stuffing/resume-analysis-sys-prompt.st")
    Resource resumeAnalysisSysPrompt;

    public ChatController(@Qualifier("openAiChatClient")ChatClient openAIChatClient, @Qualifier("ollamaChatClient")ChatClient ollamaChatClient
    , @Qualifier("ollamaChatClientWithOptions")ChatClient ollamaChatClientWithOptions){
        this.openAiChatClient = openAIChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.ollamaChatClientWithOptions = ollamaChatClientWithOptions;
    }


    @GetMapping("/gpt/chat")
    public String callOpenAI(@RequestParam("message") String message) {
        return openAiChatClient.prompt(message).call().content();
    }

    @GetMapping("/ollama/chat")
    public String callOllama(@RequestParam("message") String message) {
        return ollamaChatClient.prompt(message).call().content();
    }

    @GetMapping("/ollama/hr-chat")
    public String callOllamaHrChat(@RequestParam("message") String message) {
        return ollamaChatClient.prompt()
                .system(hrPoliciesSysPrompt)
                .user(message)
                //.advisors(new SimpleLoggerAdvisor())
                //.advisors(new TokenUsageAdvisor())
                .call()
                .content();
    }

    @GetMapping("/ollama/resume-chat")
    public String callOllamaResumeAnalysisChat(@RequestParam("message") String message) {
        return ollamaChatClient.prompt()
                .system(resumeAnalysisSysPrompt)
                .user(message)
                //.advisors(new SimpleLoggerAdvisor())
                //.advisors(new TokenUsageAdvisor())
                .call()
                .content();
    }

    @GetMapping("/ollama/resume-chat-v2")
    public String callOllamaResumeAnalysisChatWithOptions(@RequestParam("message") String message) {
        return ollamaChatClientWithOptions.prompt()
                .system(resumeAnalysisSysPrompt)
                .user(message)
                //.advisors(new SimpleLoggerAdvisor())
                //.advisors(new TokenUsageAdvisor())
                .call()
                .content();
    }

    @GetMapping("/ollama/resume-stream-resp")
    public Flux<String> callOllamaStreamResponse(@RequestParam("message") String message) {
        return ollamaChatClient.prompt()
                .system(resumeAnalysisSysPrompt)
                .user(message)
                .stream()
                .content();
        //aata hach response DB madhey takun bagh kay hotay RDBMS madhey TODO:
    }

}
