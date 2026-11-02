package com.bondtradex.ai.model;

import java.time.Instant;

public record ToolEvidence(
        String toolName,
        boolean success,
        String result,
        Instant timestamp
) {}