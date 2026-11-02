package com.bondtradex.ai.service;

import com.bondtradex.ai.tools.ServiceHealthTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ToolCallingService {

    private final ChatClient chatClient;
    private final ServiceHealthTools serviceHealthTools;

    public ToolCallingService(
            ChatClient.Builder chatClientBuilder,
            ServiceHealthTools serviceHealthTools) {

        this.chatClient = chatClientBuilder.build();
        this.serviceHealthTools = serviceHealthTools;
    }

    public String ask(String question) {

        return chatClient
                .prompt()
                .system("""
                    You are a BondTradeX learning assistant.
                    Use the getServiceHealth tool when the user
                    asks about service health or availability.

                    Tool results are simulated learning data,
                    not actual production health information.

                    If the user asks a general technical question,
                    answer without calling tools.
                    """)
                .user(question)
                .options(OllamaChatOptions.builder()
                        .model("qwen2.5:3b")
                        .temperature(0.1))
                .tools(serviceHealthTools)
                .call()
                .content();
    }
}