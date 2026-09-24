package com.bondtradex.ai.client.ollama;

import java.util.List;

import tools.jackson.databind.JsonNode;

public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream,
        JsonNode format
) {
}