package com.bondtradex.ai.controller;

import com.bondtradex.ai.service.ControlledInvestigationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/agent")
public class ControlledInvestigationController {

    private final ControlledInvestigationService service;

    public ControlledInvestigationController(
            ControlledInvestigationService service) {
        this.service = service;
    }

    public record InvestigationRequest(
            @NotBlank String serviceName
    ) {}

    @PostMapping("/controlled-investigate")
    public String investigate(
            @Valid @RequestBody InvestigationRequest request) {

        return service.investigate(request.serviceName());
    }
}