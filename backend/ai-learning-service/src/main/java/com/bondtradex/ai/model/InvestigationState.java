package com.bondtradex.ai.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InvestigationState {

    private final String serviceName;
    private final int maxToolCalls;
    private final List<ToolEvidence> evidence = new ArrayList<>();

    private int toolCalls;
    private InvestigationStatus status = InvestigationStatus.CREATED;

    public boolean hasFailedEvidence() {
        return evidence.stream()
                .anyMatch(item -> !item.success());
    }

    public void finish() {
        if (status != InvestigationStatus.RUNNING) {
            throw new IllegalStateException(
                    "Investigation is not running"
            );
        }

        status = hasFailedEvidence()
                ? InvestigationStatus.PARTIALLY_COMPLETED
                : InvestigationStatus.COMPLETED;
    }

    public InvestigationState(String serviceName, int maxToolCalls) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (maxToolCalls < 1) {
            throw new IllegalArgumentException("Tool budget must be positive");
        }

        this.serviceName = serviceName;
        this.maxToolCalls = maxToolCalls;
    }

    public void start() {
        if (status != InvestigationStatus.CREATED) {
            throw new IllegalStateException("Investigation already started");
        }
        status = InvestigationStatus.RUNNING;
    }

    public void reserveToolCall() {
        if (status != InvestigationStatus.RUNNING) {
            throw new IllegalStateException("Investigation is not running");
        }
        if (toolCalls >= maxToolCalls) {
            throw new IllegalStateException("Tool budget exhausted");
        }
        toolCalls++;
    }

    public void recordEvidence(
            String toolName, boolean success, String result) {

        evidence.add(new ToolEvidence(
                toolName, success, result, Instant.now()
        ));
    }

    public void complete() {
        if (status != InvestigationStatus.RUNNING) {
            throw new IllegalStateException("Investigation is not running");
        }
        status = InvestigationStatus.COMPLETED;
    }

    public void fail() {
        if (status == InvestigationStatus.RUNNING) {
            status = InvestigationStatus.FAILED;
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getToolCalls() {
        return toolCalls;
    }

    public int remainingToolCalls() {
        return maxToolCalls - toolCalls;
    }

    public InvestigationStatus getStatus() {
        return status;
    }

    public List<ToolEvidence> getEvidence() {
        return List.copyOf(evidence);
    }
}