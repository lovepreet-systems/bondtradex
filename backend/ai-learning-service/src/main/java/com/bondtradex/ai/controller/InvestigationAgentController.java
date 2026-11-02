package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.RagRequest;
import com.bondtradex.ai.service.InvestigationAgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/agent")
public class InvestigationAgentController {

    private final InvestigationAgentService agentService;

    public InvestigationAgentController(InvestigationAgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/investigate")
    public String investigate(@Valid @RequestBody RagRequest request) {
        return agentService.investigate(request.question());
    }
}