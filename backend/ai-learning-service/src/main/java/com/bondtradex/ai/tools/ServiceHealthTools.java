package com.bondtradex.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ServiceHealthTools {

    private static final Logger log =
            LoggerFactory.getLogger(ServiceHealthTools.class);

    @Tool(
            name = "getServiceHealth",
            description = "Get the simulated health status of a BondTradeX service. " +
                    "Use this tool when asked about service health or availability."
    )
    public String getServiceHealth(
            @ToolParam(description = "Service name, such as ioi-service, auth-service, or api-gateway")
            String serviceName) {
        log.info("TOOL EXECUTED: getServiceHealth({})", serviceName);
        return switch (serviceName) {
            case "ioi-service" -> "ioi-service is DOWN. Reason: Readiness probe failed.";
            case "auth-service" -> "auth-service is UP.";
            case "api-gateway" -> "api-gateway is UP.";
            default -> "Unknown service: " + serviceName;
        };
    }

    @Tool(
            name = "getRecentLogs",
            description = "Get simulated recent application logs for a BondTradeX service. " +
                    "Use when investigating failures, exceptions, crashes, or readiness problems."
    )
    public String getRecentLogs(
            @ToolParam(description = "BondTradeX service name")
            String serviceName) {

        log.info("TOOL EXECUTED: getRecentLogs({})", serviceName);
        if ("ioi-service".equals(serviceName)) {
            throw new IllegalStateException(
                    "SIMULATED FAILURE: Log provider unavailable"
            );
        }

        return switch (serviceName) {
            case "ioi-service" -> """
            SIMULATED LOGS for ioi-service:
            ERROR java.lang.OutOfMemoryError: Java heap space
            WARN Readiness health check failed
            ERROR Request processing stopped due to memory pressure
            """;
            case "auth-service" -> "SIMULATED LOGS: No recent errors.";
            case "api-gateway" -> "SIMULATED LOGS: No recent errors.";
            default -> "Unknown service: " + serviceName;
        };
    }

    @Tool(
            name = "getRecentDeployments",
            description = "Get simulated recent deployment history for a BondTradeX service. " +
                    "Use when investigating whether a recent release may be related to an incident."
    )
    public String getRecentDeployments(
            @ToolParam(description = "BondTradeX service name")
            String serviceName) {

        log.info("TOOL EXECUTED: getRecentDeployments({})", serviceName);

        return switch (serviceName) {
            case "ioi-service" -> """
            SIMULATED DEPLOYMENT HISTORY for ioi-service:
            Version: 2.7
            Deployed: 15 minutes before the simulated incident
            Previous version: 2.6
            Deployment result: Completed
            """;
            case "auth-service" -> "SIMULATED: No recent deployments.";
            case "api-gateway" -> "SIMULATED: No recent deployments.";
            default -> "Unknown service: " + serviceName;
        };
    }
}