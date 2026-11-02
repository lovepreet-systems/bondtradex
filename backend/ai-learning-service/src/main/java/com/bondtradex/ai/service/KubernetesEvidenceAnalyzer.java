
package com.bondtradex.ai.service;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class KubernetesEvidenceAnalyzer {

    public String analyze(
            JsonNode pod,
            JsonNode events
    ) {
        boolean running =
                "Running".equals(
                        pod.path("phase").asText()
                );

        boolean ready =
                pod.path("podReady").asBoolean();

        int restartCount =
                pod.path("restartCount").asInt();

        boolean readinessFailure = false;
        String probeFailureMessage = "";

        if (events.isArray()) {
            for (JsonNode event : events) {
                String reason =
                        event.path("reason").asText("");

                String message =
                        event.path("message").asText("");

                if ("Unhealthy".equals(reason)
                        && message.contains(
                        "Readiness probe failed"
                )) {
                    readinessFailure = true;
                    probeFailureMessage = message;
                    break;
                }
            }
        }

        StringBuilder facts = new StringBuilder();

        facts.append("VERIFIED KUBERNETES FACTS:\n");

        facts.append("- Pod phase: ")
                .append(running ? "Running" :
                        pod.path("phase").asText())
                .append("\n");

        facts.append("- Pod ready: ")
                .append(ready)
                .append("\n");

        facts.append("- Container restarts: ")
                .append(restartCount)
                .append("\n");

        if (running && !ready) {
            facts.append(
                    "- The pod is running but not ready.\n"
            );
        }

        if (restartCount == 0) {
            facts.append(
                    "- No container restarts were recorded.\n"
            );
        }

        if (readinessFailure) {
            facts.append(
                    "- Kubernetes reported a readiness " +
                            "probe failure.\n"
            );

            facts.append("- Probe error: ")
                    .append(probeFailureMessage)
                    .append("\n");
        }

        return facts.toString();
    }


    public String analyzeConfiguration(
            JsonNode pod,
            JsonNode events,
            JsonNode configuration
    ) {
        StringBuilder findings = new StringBuilder();

        findings.append("VERIFIED CONFIGURATION FINDINGS:\n");

        boolean podReady =
                pod.path("podReady").asBoolean();

        int restarts =
                pod.path("restartCount").asInt();

        findings.append("- Pod ready: ")
                .append(podReady).append("\n");

        findings.append("- Restart count: ")
                .append(restarts).append("\n");

        JsonNode containers = configuration.path("containers");

        if (!containers.isArray()) {
            return findings.append(
                    "- Container configuration unavailable.\n"
            ).toString();
        }

        for (JsonNode container : containers) {

            String containerName =
                    container.path("name").asText();

            JsonNode probe =
                    container.path("readinessProbe");

            if (!probe.path("configured").asBoolean()) {
                continue;
            }

            if (!"HTTP".equals(probe.path("type").asText())) {
                continue;
            }

            JsonNode probePortNode = probe.path("port");

            if (!probePortNode.isIntegralNumber()) {
                findings.append(
                        "- Readiness probe uses a named or " +
                                "unresolved port.\n"
                );
                continue;
            }

            int probePort = probePortNode.asInt();

            JsonNode declaredPorts = container.path("ports");

            if (!declaredPorts.isArray()
                    || declaredPorts.isEmpty()) {
                findings.append(
                        "- No container ports declared for "
                                + containerName + ".\n"
                );
                continue;
            }

            boolean matchingPort = false;

            for (JsonNode port : declaredPorts) {
                if (port.path("containerPort").asInt(-1)
                        == probePort) {
                    matchingPort = true;
                    break;
                }
            }

            if (!matchingPort) {
                findings.append(
                                "- CONFIGURATION MISMATCH: Container "
                        ).append(containerName)
                        .append(" declares ports ")
                        .append(declaredPorts.toString())
                        .append(" but its readiness probe targets ")
                        .append(probePort)
                        .append(".\n");
            }
        }

        boolean readinessWarning = false;

        if (events.isArray()) {
            for (JsonNode event : events) {
                String message =
                        event.path("message").asText("");

                if ("Unhealthy".equals(
                        event.path("reason").asText())
                        && message.contains(
                        "Readiness probe failed")) {
                    readinessWarning = true;
                    break;
                }
            }
        }

        findings.append("- Readiness failure event observed: ")
                .append(readinessWarning)
                .append("\n");

        findings.append(
                "- Declared ports are configuration metadata, " +
                        "not proof of active listening sockets.\n"
        );

        return findings.toString();
    }

}
