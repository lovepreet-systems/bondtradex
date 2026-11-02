package com.bondtradex.ai.service;

import com.bondtradex.ai.model.InvestigationState;
import com.bondtradex.ai.tools.ServiceHealthTools;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

@Component
public class InvestigationToolExecutor {

    private static final Set<String> ALLOWED_SERVICES =
            Set.of("ioi-service", "auth-service", "api-gateway");

    private final ServiceHealthTools tools;

    public InvestigationToolExecutor(ServiceHealthTools tools) {
        this.tools = tools;
    }

    public void collectEvidence(InvestigationState state) {

        String service = state.getServiceName();

        if (!ALLOWED_SERVICES.contains(service)) {
            throw new IllegalArgumentException(
                    "Unsupported service: " + service
            );
        }

        state.start();

        executeTool(state, "getServiceHealth",
                () -> tools.getServiceHealth(service), 1);

        executeTool(state, "getRecentLogs",
                () -> tools.getRecentLogs(service), 2);

        executeTool(state, "getRecentDeployments",
                () -> tools.getRecentDeployments(service), 1);
    }

    private boolean executeTool(
            InvestigationState state,
            String toolName,
            Supplier<String> toolAction,
            int maxAttempts) {

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            if (state.remainingToolCalls() == 0) {
                return false;
            }

            state.reserveToolCall();

            try {
                String result = toolAction.get();

                state.recordEvidence(
                        toolName,
                        true,
                        result
                );

                return true;

            } catch (Exception ex) {

                state.recordEvidence(
                        toolName,
                        false,
                        "Attempt " + attempt + ": " + ex.getMessage()
                );
            }
        }

        return false;
    }
}