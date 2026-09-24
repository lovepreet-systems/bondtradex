package com.bondtradex.ai.service;

import com.bondtradex.ai.client.ollama.OllamaClient;
import com.bondtradex.ai.client.ollama.OllamaMessage;
import org.springframework.stereotype.Service;
import com.bondtradex.ai.model.AnalysisResult;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;

@Service
public class AnalysisService {

    /*private static final String SYSTEM_PROMPT = """
            You are a technical analysis assistant.

            Analyze the technical issue provided by the user.

            Be concise and factual.
            If you do not have enough information, say so.
            Do not invent missing facts.
            """;*/

    private static final String SYSTEM_PROMPT = """
        You are a technical analysis assistant.

        Analyze the technical issue provided by the user.

        Return ONLY valid JSON.
        Do not include markdown.
        Do not include ```json code fences.
        Do not include any text before or after the JSON.

        The JSON must have exactly this structure:

        {
          "summary": "short summary of the issue",
          "category": "technical category",
          "severity": "LOW, MEDIUM, or HIGH",
          "suggestions": [
            "suggestion 1",
            "suggestion 2"
          ]
        }

        Rules:
        - summary must be concise.
        - category must be a short technical category.
        - severity must be exactly LOW, MEDIUM, or HIGH.
        - suggestions must be a JSON array of strings.
        - If there is not enough information, do not invent missing facts.
        """;

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;


    public AnalysisService(
            OllamaClient ollamaClient,
            ObjectMapper objectMapper
    ) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public AnalysisResult analyze(String text) throws IOException, InterruptedException {

        List<OllamaMessage> messages = List.of(
                new OllamaMessage("system", SYSTEM_PROMPT),
                new OllamaMessage("user", text)
        );

        String response = ollamaClient.chat(messages,createOutputSchema());
        System.out.println("RAW MODEL RESPONSE:");
        System.out.println(response);
        return objectMapper.readValue(response, AnalysisResult.class);
    }

    private JsonNode createOutputSchema()
             {

        return objectMapper.readTree("""
            {
              "type": "object",
              "properties": {
                "summary": {
                  "type": "string"
                },
                "category": {
                  "type": "string"
                },
                "severity": {
                  "type": "string",
                  "enum": ["LOW", "MEDIUM", "HIGH"]
                },
                "suggestions": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                }
              },
              "required": [
                "summary",
                "category",
                "severity",
                "suggestions"
              ]
            }
            """);
    }
}