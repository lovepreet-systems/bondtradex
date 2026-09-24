package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.AnalysisResult;
import com.bondtradex.ai.model.AnalyzeRequest;
import com.bondtradex.ai.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyze(
            @Valid @RequestBody AnalyzeRequest request
    ) throws IOException, InterruptedException {

        AnalysisResult result = analysisService.analyze(request.text());

        return ResponseEntity.ok(result);
    }
}