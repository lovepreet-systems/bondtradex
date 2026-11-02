package com.bondtradex.ai.controller;

import com.bondtradex.ai.service.McpChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/mcp")
public class McpChatController {

    private final McpChatService service;

    public McpChatController(McpChatService service) {
        this.service = service;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> request) {

        String answer = service.ask(request.get("question"));

        return Map.of("answer", answer);
    }
}