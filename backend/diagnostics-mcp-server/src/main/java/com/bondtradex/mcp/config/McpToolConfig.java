package com.bondtradex.mcp.config;

import com.bondtradex.mcp.tools.KubernetesDiagnosticsTools;
import com.bondtradex.mcp.tools.ServiceDiagnosticsTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider diagnosticTools(
            ServiceDiagnosticsTools serviceTools,
            KubernetesDiagnosticsTools kubernetesTools) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(serviceTools, kubernetesTools)
                .build();
    }
}