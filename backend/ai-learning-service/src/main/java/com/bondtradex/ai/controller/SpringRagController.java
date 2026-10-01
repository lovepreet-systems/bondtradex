package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.RagRequest;
import com.bondtradex.ai.service.SpringRagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/spring-rag")
public class SpringRagController {

    private final SpringRagService springRagService;

    public SpringRagController(
            SpringRagService springRagService
    ) {
        this.springRagService = springRagService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(
            @Valid @RequestBody RagRequest request
    ) {
        return ResponseEntity.ok(
                springRagService.ask(request.question())
        );
    }
}