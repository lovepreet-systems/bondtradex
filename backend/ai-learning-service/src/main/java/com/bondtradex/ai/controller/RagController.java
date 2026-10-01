package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.RagRequest;
import com.bondtradex.ai.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bondtradex.ai.model.IncidentRequest;

@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(
            @Valid @RequestBody RagRequest request
    ) {

        String answer = ragService.ask(request.question());

        return ResponseEntity.ok(answer);
    }

    @PostMapping("/incidents")
    public ResponseEntity<Void> ingestIncident(
            @Valid @RequestBody IncidentRequest request
    ) {
        ragService.ingestIncident(request);
        return ResponseEntity.ok().build();
    }
}