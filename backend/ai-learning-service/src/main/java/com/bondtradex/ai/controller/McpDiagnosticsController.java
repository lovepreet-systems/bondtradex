package com.bondtradex.ai.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/mcp")
public class McpDiagnosticsController {

    private final List<McpSyncClient> clients;

    public McpDiagnosticsController(List<McpSyncClient> clients) {
        this.clients = clients;
    }

    @GetMapping("/tools")
    public List<String> listTools() {
        return clients.stream()
                .flatMap(client ->
                        client.listTools().tools().stream())
                .map(tool -> tool.name())
                .toList();
    }

    @GetMapping("/health")
    public Object getHealth(@RequestParam String serviceName) {

        if (clients.isEmpty()) {
            throw new IllegalStateException("No MCP clients configured");
        }
        McpSyncClient client = clients.getFirst();
        return client.callTool(new McpSchema.CallToolRequest("getServiceHealth", Map.of("serviceName", serviceName)));
    }

    @GetMapping("/pods")
    public Object getPods(
            @RequestParam(defaultValue = "kube-system")
            String namespace) {

        if (clients.isEmpty()) {
            throw new IllegalStateException(
                    "No MCP clients configured"
            );
        }

        McpSyncClient client = clients.getFirst();

        return client.callTool(
                new McpSchema.CallToolRequest(
                        "getKubernetesPods",
                        Map.of("namespace", namespace)
                )
        );
    }

    @GetMapping("/events")
    public Object getEvents(
            @RequestParam(defaultValue = "kube-system")
            String namespace) {

        if (clients.isEmpty()) {
            throw new IllegalStateException(
                    "No MCP clients configured"
            );
        }

        McpSyncClient client = clients.getFirst();

        return client.callTool(
                new McpSchema.CallToolRequest(
                        "getKubernetesEvents",
                        Map.of("namespace", namespace)
                )
        );
    }


    @GetMapping("/logs")
    public Object getPodLogs(
            @RequestParam(defaultValue = "kube-system") String namespace,
            @RequestParam String podName) {

        if (clients.isEmpty()) {
            throw new IllegalStateException("No MCP clients configured");
        }

        return clients.getFirst().callTool(
                new McpSchema.CallToolRequest(
                        "getPodLogs",
                        Map.of(
                                "namespace", namespace,
                                "podName", podName
                        )
                )
        );
    }


    @GetMapping("/configuration")
    public Object getPodConfiguration(
            @RequestParam String namespace,
            @RequestParam String podName
    ) {
        return clients.getFirst().callTool(
                new McpSchema.CallToolRequest(
                        "getPodConfiguration",
                        Map.of(
                                "namespace", namespace,
                                "podName", podName
                        )
                )
        );
    }
}