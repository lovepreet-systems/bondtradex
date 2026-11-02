package com.bondtradex.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ServiceDiagnosticsTools {

    private static final Logger log =
            LoggerFactory.getLogger(ServiceDiagnosticsTools.class);

    @Tool(
            name = "getServiceHealth",
            description = "Get simulated health information for a BondTradeX service"
    )
    public String getServiceHealth(
            @ToolParam(description = "BondTradeX service name")
            String serviceName) {

        log.info("MCP TOOL EXECUTED: getServiceHealth({})", serviceName);

        return switch (serviceName) {

            case "ioi-service" ->
                    "SIMULATED: ioi-service is DOWN. Readiness probe failed.";

            case "auth-service" ->
                    "SIMULATED: auth-service is UP.";

            case "api-gateway" ->
                    "SIMULATED: api-gateway is UP.";

            default ->
                    "SIMULATED: Unknown service: " + serviceName;
        };
    }
}