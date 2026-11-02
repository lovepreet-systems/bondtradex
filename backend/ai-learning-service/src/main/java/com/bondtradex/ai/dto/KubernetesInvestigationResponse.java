package com.bondtradex.ai.dto;

import tools.jackson.databind.JsonNode;

public record KubernetesInvestigationResponse(
        String namespace,
        String deploymentName,
        String podName,
        JsonNode podStatus,
        JsonNode events,
        JsonNode logs,
        JsonNode configuration,
        String verifiedFindings,
        String diagnosis
) {
}
