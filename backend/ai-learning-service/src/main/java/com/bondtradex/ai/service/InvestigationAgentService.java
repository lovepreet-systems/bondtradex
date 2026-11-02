package com.bondtradex.ai.service;

import com.bondtradex.ai.tools.ServiceHealthTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

@Service
public class InvestigationAgentService {

    private final ChatClient chatClient;
    private final ServiceHealthTools tools;

    public InvestigationAgentService(
            ChatClient.Builder builder,
            ServiceHealthTools tools) {

        this.chatClient = builder.build();
        this.tools = tools;
    }

    public String investigate(String question) {

        return chatClient.prompt()
                .system("""
                    You are a read-only BondTradeX incident investigation assistant.

                    Investigate service incidents using the available tools.

                    For a service failure investigation:
                    1. Check service health.
                    2. Inspect recent application logs.
                    3. Inspect recent deployment history.
                    4. Correlate the evidence.

                    Clearly distinguish:
                    - Observed facts
                    - Likely causes
                    - Recommended next steps

                    All tool results are SIMULATED.
                    Never describe them as actual production observations.

                    Do not invent tool results or claim an unverified root cause.
                    Never perform restarts, rollbacks, or deployments.
                    """)
                .user(question)
                .options(OllamaChatOptions.builder()
                        .model("qwen2.5:3b")
                        .temperature(0.1))
                .tools(tools)
                .call()
                .content();
    }
}