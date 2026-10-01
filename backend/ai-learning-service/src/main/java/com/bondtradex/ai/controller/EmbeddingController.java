package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.AnalyzeRequest;
import com.bondtradex.ai.model.SearchResult;
import com.bondtradex.ai.model.SimilarityRequest;
import com.bondtradex.ai.service.EmbeddingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping
    public ResponseEntity<float[]> embed(
            @Valid @RequestBody AnalyzeRequest request
    ) {

        float[] embedding = embeddingService.embed(request.text());
        System.out.println(
                "Embedding dimensions: " + embedding.length
        );
        return ResponseEntity.ok(embedding);
    }

    @PostMapping("/similarity")
    public ResponseEntity<Double> similarity(
            @Valid @RequestBody SimilarityRequest request
    ) {

        double similarity = embeddingService.compare(
                request.textA(),
                request.textB()
        );

        return ResponseEntity.ok(similarity);
    }

    @PostMapping("/search")
    public ResponseEntity<List<SearchResult>> search(
            @Valid @RequestBody AnalyzeRequest request
    ) {

        List<SearchResult> results =
                embeddingService.search(request.text());

        return ResponseEntity.ok(results);
    }
}