package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.RagRequest;
import com.bondtradex.ai.service.ToolCallingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/tools")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    @PostMapping("/ask")
    public String ask(@Valid @RequestBody RagRequest request) {
        return toolCallingService.ask(request.question());
    }
}