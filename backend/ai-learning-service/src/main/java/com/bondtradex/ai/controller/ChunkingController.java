package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.ChunkRequest;
import com.bondtradex.ai.service.ChunkingService;
import com.bondtradex.ai.service.TokenChunkingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.document.Document;
import java.util.List;

@RestController
@RequestMapping("/api/ai/chunks")
public class ChunkingController {

    private final ChunkingService chunkingService;
    private final TokenChunkingService tokenChunkingService;

    public ChunkingController(
            ChunkingService chunkingService,
            TokenChunkingService tokenChunkingService
    ) {
        this.chunkingService = chunkingService;
        this.tokenChunkingService = tokenChunkingService;
    }

    @PostMapping
    public ResponseEntity<List<String>> chunk(@Valid @RequestBody ChunkRequest request) {

        List<String> chunks = chunkingService.chunk(request.text(), request.chunkSize(), request.overlap());

        return ResponseEntity.ok(chunks);
    }

    @PostMapping("/token")
    public ResponseEntity<List<String>> tokenChunk(
            @Valid @RequestBody ChunkRequest request
    ) {

        List<String> chunks = tokenChunkingService.chunk(request.text()).stream().map(Document::getText).toList();

        return ResponseEntity.ok(chunks);
    }
}