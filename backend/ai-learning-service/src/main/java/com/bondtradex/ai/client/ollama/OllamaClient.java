package com.bondtradex.ai.client.ollama;

import com.bondtradex.ai.config.OllamaProperties;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class OllamaClient {

    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaClient(
            OllamaProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String chat(
            List<OllamaMessage> messages,
            JsonNode format
    ) throws IOException, InterruptedException {

        OllamaChatRequest chatRequest = new OllamaChatRequest(
                properties.getModel(),
                messages,
                false,
                format
        );

        String requestJson = objectMapper.writeValueAsString(chatRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        OllamaChatResponse chatResponse =
                objectMapper.readValue(
                        response.body(),
                        OllamaChatResponse.class
                );

        return chatResponse.message().content();
    }
}