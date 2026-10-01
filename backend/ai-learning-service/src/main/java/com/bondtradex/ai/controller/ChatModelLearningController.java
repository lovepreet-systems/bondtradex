package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.RagRequest;
import com.bondtradex.ai.service.ChatModelLearningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai/chat-model")
public class ChatModelLearningController {

    private final ChatModelLearningService service;

    public ChatModelLearningController(
            ChatModelLearningService service
    ) {
        this.service = service;
    }

    @PostMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> stream(
            @Valid @RequestBody RagRequest request
    ) {
        return service.stream(request.question());
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(
            @Valid @RequestBody RagRequest request
    ) {
        return ResponseEntity.ok(service.ask(request.question())
        );
    }
}