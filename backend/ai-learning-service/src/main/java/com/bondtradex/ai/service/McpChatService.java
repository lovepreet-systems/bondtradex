package com.bondtradex.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class McpChatService {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public McpChatService(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider mcpTools) {

        this.chatClient = chatClientBuilder.build();
        this.mcpTools = mcpTools;
    }

    public String ask(String question) {

        return chatClient.prompt()
                .system("""
                    You are a BondTradeX service diagnostics assistant.
                    Use available diagnostic tools when the user asks
                    about service health.

                    Never claim simulated data is real production data.
                    Do not invent service status.
                    """)
                .user(question)
                .options(OllamaChatOptions.builder()
                        .model("qwen2.5:3b")
                        .temperature(0.1))
                .tools(mcpTools.getToolCallbacks())
                .call()
                .content();
    }
}