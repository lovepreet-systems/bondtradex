package com.bondtradex.ai.client.ollama;

public record OllamaMessage(
        String role,
        String content
) {
}