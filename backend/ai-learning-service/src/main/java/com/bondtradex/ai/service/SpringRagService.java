package com.bondtradex.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class SpringRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public SpringRagService(
            ChatClient chatClient,
            VectorStore vectorStore
    ) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .topK(3)
                        .similarityThreshold(0.7)
                        .build();

        QuestionAnswerAdvisor advisor =
                QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build();

        return chatClient
                .prompt()
                .user(question)
                .advisors(advisor)
                .call()
                .content();
    }
}