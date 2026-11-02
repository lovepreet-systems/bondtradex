package com.bondtradex.ai.dto;

public record KubernetesInvestigationRequest(
        String namespace,
        String deploymentName
) {
}