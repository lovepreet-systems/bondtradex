package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.DocumentRequest;
import com.bondtradex.ai.service.VectorDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bondtradex.ai.model.VectorSearchResult;
import java.util.List;

@RestController
@RequestMapping("/api/ai/documents")
public class VectorDocumentController {

    private final VectorDocumentService vectorDocumentService;

    public VectorDocumentController(
            VectorDocumentService vectorDocumentService
    ) {
        this.vectorDocumentService = vectorDocumentService;
    }

    @PostMapping
    public ResponseEntity<String> addDocument(
            @Valid @RequestBody DocumentRequest request
    ) {

        vectorDocumentService.addDocument(request);

        return ResponseEntity.ok().body("CREATED");
    }

    @GetMapping("/search")
    public ResponseEntity<List<VectorSearchResult>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK
    ) {

        List<VectorSearchResult> results =
                vectorDocumentService.search(query, topK);

        return ResponseEntity.ok(results);
    }
}