package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.DocumentRequest;
import com.bondtradex.ai.service.SpringVectorStoreService;
import jakarta.validation.Valid;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/vector-store")
public class SpringVectorStoreController {

    private final SpringVectorStoreService vectorStoreService;

    public SpringVectorStoreController(
            SpringVectorStoreService vectorStoreService
    ) {
        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/documents")
    public ResponseEntity<Void> addDocument(
            @Valid @RequestBody DocumentRequest request
    ) {
        vectorStoreService.addDocument(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK
    ) {
        return ResponseEntity.ok(
                vectorStoreService.search(query, topK)
        );
    }

    @GetMapping("/search/service")
    public ResponseEntity<List<Document>> searchByService(
            @RequestParam String query,
            @RequestParam String service,
            @RequestParam(defaultValue = "3") int topK
    ) {
        return ResponseEntity.ok(
                vectorStoreService.searchByService(
                        query,
                        service,
                        topK
                )
        );
    }
}