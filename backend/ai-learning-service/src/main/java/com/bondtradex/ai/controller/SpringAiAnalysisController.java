package com.bondtradex.ai.controller;

import com.bondtradex.ai.model.AnalysisResult;
import com.bondtradex.ai.model.AnalyzeRequest;
import com.bondtradex.ai.service.SpringAiAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/spring")
public class SpringAiAnalysisController {

    private final SpringAiAnalysisService analysisService;

    public SpringAiAnalysisController(
            SpringAiAnalysisService analysisService
    ) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyze(
            @Valid @RequestBody AnalyzeRequest request
    ) {

        AnalysisResult result = analysisService.analyze(request.text());

        return ResponseEntity.ok(result);
    }
}