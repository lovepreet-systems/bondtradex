package com.bondtradex.ai.service;

import com.bondtradex.ai.model.InvestigationState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import com.bondtradex.ai.model.ToolEvidence;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class ControlledInvestigationService {

    private static final Logger log =
            LoggerFactory.getLogger(ControlledInvestigationService.class);
    private final ChatClient chatClient;
    private final InvestigationToolExecutor toolExecutor;

    public ControlledInvestigationService(
            ChatClient.Builder builder,
            InvestigationToolExecutor toolExecutor) {

        this.chatClient = builder.build();
        this.toolExecutor = toolExecutor;
    }

    public String investigate(String serviceName) {

        InvestigationState state =
                new InvestigationState(serviceName, 4);

        try {
            toolExecutor.collectEvidence(state);

            log.info(
                    "Investigation state after tools: status={}, toolCalls={}, evidenceCount={}",
                    state.getStatus(),
                    state.getToolCalls(),
                    state.getEvidence().size()
            );

            // Keep your existing evidence formatting code here.
            String evidence = state.getEvidence()
                    .stream()
                    .map(item -> """
                        Tool: %s
                        Success: %s
                        Result: %s
                        Timestamp: %s
                        """.formatted(
                            item.toolName(),
                            item.success(),
                            item.result(),
                            item.timestamp()
                    ))
                    .collect(java.util.stream.Collectors.joining("\n\n"));

            // Keep your existing complete system prompt here.
            String response = chatClient.prompt()
                    .system("""
                    You are a read-only BondTradeX incident analyst.
                    Analyze the provided SIMULATED evidence.

                    Structure your answer into:
                    1. Observed facts
                    2. Likely root cause
                    3. Recommended next steps

                    Use only the provided evidence for incident facts.
                    Do not invent logs, deployments, or metrics.
                    A recent deployment is correlation, not proof of causation.
                    Distinguish readiness failures from underlying causes.
                    Never claim to have executed recovery actions.
                    Explicitly state that the evidence is simulated.
                    """)
                    .user("""
                    Investigate service: %s

                    Collected evidence:
                    %s
                    """.formatted(serviceName, evidence))
                    .options(OllamaChatOptions.builder()
                            .model("qwen2.5:3b")
                            .temperature(0.1))
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("LLM returned an empty investigation report");
            }

            state.finish();

            log.info(
                    "Investigation finished: service={}, status={}, toolCalls={}, evidenceCount={}",
                    state.getServiceName(),
                    state.getStatus(),
                    state.getToolCalls(),
                    state.getEvidence().size()
            );
            return response;

        } catch (Exception ex) {

            state.fail();

            log.error(
                    "Investigation failed: service={}, status={}, toolCalls={}",
                    state.getServiceName(),
                    state.getStatus(),
                    state.getToolCalls(),
                    ex
            );

            throw ex;
        }
    }
}