package com.bondtradex.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatModelLearningService {

    private final ChatModel chatModel;
    private final ChatClient chatClient;

    public ChatModelLearningService(
            ChatModel chatModel,
            ChatClient chatClient
    ) {
        this.chatModel = chatModel;
        this.chatClient = chatClient;
    }

    public Flux<String> stream(String question) {

        return chatClient
                .prompt()
                .system(
                        "You are a Java production support assistant. " +
                                "Answer concisely and technically."
                )
                .user(question)
                .stream()
                .content();
    }

    public String ask(String question) {

        SystemMessage systemMessage =
                new SystemMessage(
                        "You are a Java production support assistant. " +
                                "Answer concisely and technically."
                );

        UserMessage userMessage = new UserMessage(question);

        OllamaChatOptions options =
                OllamaChatOptions.builder()
                        .model("gemma3:1b")
                        .temperature(0.2)
                        .build();

        Prompt prompt =
                new Prompt(
                        List.of(
                                systemMessage,
                                userMessage
                        ),
                        options
                );

        ChatResponse response =
                chatModel.call(prompt);

        return response
                .getResult()
                .getOutput()
                .getText();
    }
}