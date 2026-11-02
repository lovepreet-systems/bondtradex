
package com.bondtradex.ai.controller;

import com.bondtradex.ai.dto.KubernetesInvestigationRequest;
import com.bondtradex.ai.dto.KubernetesInvestigationResponse;
import com.bondtradex.ai.service.ControlledKubernetesInvestigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/agent/kubernetes")
public class ControlledKubernetesInvestigationController {

    private final ControlledKubernetesInvestigationService service;

    public ControlledKubernetesInvestigationController(
            ControlledKubernetesInvestigationService service
    ) {
        this.service = service;
    }

    @PostMapping("/investigate")
    public ResponseEntity<KubernetesInvestigationResponse> investigate(
            @RequestBody KubernetesInvestigationRequest request
    ) {
        return ResponseEntity.ok(service.investigate(request));
    }
}
