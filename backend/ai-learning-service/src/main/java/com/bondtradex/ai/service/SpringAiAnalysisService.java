package com.bondtradex.ai.service;

import com.bondtradex.ai.model.AnalysisResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAiAnalysisService {

    private static final String SYSTEM_PROMPT = """
            You are a technical analysis assistant.

            Analyze the technical issue provided by the user.

            Be concise and factual.
            If you do not have enough information, say so.
            Do not invent missing facts.
            """;

    private final ChatClient chatClient;

    public SpringAiAnalysisService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AnalysisResult analyze(String text) {

        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(text)
                .call()
                .entity(                AnalysisResult.class,
                        spec -> spec
                                .useProviderStructuredOutput()
                                .validateSchema()
                );
    }
}