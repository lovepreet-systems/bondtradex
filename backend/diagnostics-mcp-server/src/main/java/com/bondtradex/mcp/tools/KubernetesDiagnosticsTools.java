package com.bondtradex.mcp.tools;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.Event;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.HTTPGetAction;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Probe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KubernetesDiagnosticsTools {

    private static final Pattern POD_NAME_PATTERN =
            Pattern.compile(
                    "^[a-z0-9]([-a-z0-9.]*[a-z0-9])?$"
            );

    private static final Logger log =
            LoggerFactory.getLogger(KubernetesDiagnosticsTools.class);

    private static final Set<String> ALLOWED_NAMESPACES = Set.of("kube-system", "default", "ai-sre-lab");

    private final KubernetesClient client;

    public KubernetesDiagnosticsTools(KubernetesClient client) {
        this.client = client;
    }


    @Tool(description = """
        Retrieve Kubernetes Pod container ports and health probes.
        Read-only. Does not modify Kubernetes resources.
        """)
    public Map<String, Object> getPodConfiguration(
            String namespace,
            String podName
    ) {
        validateNamespace(namespace);
        validatePodName(podName);

        log.info(
                "MCP TOOL EXECUTED: getPodConfiguration({}, {})",
                namespace,
                podName
        );

        Pod pod = client.pods()
                .inNamespace(namespace)
                .withName(podName)
                .get();

        if (pod == null) {
            throw new IllegalArgumentException(
                    "Pod not found: " + podName
            );
        }

        List<Map<String, Object>> containers =
                new ArrayList<>();

        for (Container container : pod.getSpec().getContainers()) {

            List<Map<String, Object>> ports =
                    new ArrayList<>();

            if (container.getPorts() != null) {
                for (ContainerPort port : container.getPorts()) {
                    ports.add(Map.of(
                            "containerPort",
                            port.getContainerPort(),
                            "protocol",
                            port.getProtocol() == null
                                    ? "TCP"
                                    : port.getProtocol()
                    ));
                }
            }

            Map<String, Object> containerInfo =
                    new LinkedHashMap<>();

            containerInfo.put("name", container.getName());
            containerInfo.put("image", container.getImage());
            containerInfo.put("ports", ports);

            containerInfo.put(
                    "readinessProbe",
                    describeProbe(container.getReadinessProbe())
            );

            containerInfo.put(
                    "livenessProbe",
                    describeProbe(container.getLivenessProbe())
            );

            containers.add(containerInfo);
        }

        return Map.of(
                "namespace", namespace,
                "podName", podName,
                "containers", containers
        );
    }


    @Tool(
            name = "getPodLogs",
            description = "Read a bounded number of recent log lines from a pod in an approved Kubernetes namespace"
    )
    public Map<String, Object> getPodLogs(
            @ToolParam(description = "Approved Kubernetes namespace")
            String namespace,

            @ToolParam(description = "Exact Kubernetes pod name")
            String podName) {

        if (!ALLOWED_NAMESPACES.contains(namespace)) {
            throw new IllegalArgumentException(
                    "Namespace not allowed: " + namespace
            );
        }

        if (podName == null
                || podName.length() > 253
                || !POD_NAME_PATTERN.matcher(podName).matches()) {

            throw new IllegalArgumentException("Invalid pod name");
        }

        log.info(
                "MCP TOOL EXECUTED: getPodLogs({}, {})",
                namespace,
                podName
        );

        Pod pod = client.pods()
                .inNamespace(namespace)
                .withName(podName)
                .get();

        if (pod == null) {
            throw new IllegalArgumentException(
                    "Pod not found: " + podName
            );
        }

        if (pod.getSpec() == null
                || pod.getSpec().getContainers() == null
                || pod.getSpec().getContainers().size() != 1) {

            throw new IllegalArgumentException(
                    "This learning tool currently supports single-container pods only"
            );
        }

        String containerName =
                pod.getSpec().getContainers().getFirst().getName();

        String logs = client.pods()
                .inNamespace(namespace)
                .withName(podName)
                .inContainer(containerName)
                .tailingLines(50)
                .getLog();

        int maxCharacters = 8000;

        if (logs != null && logs.length() > maxCharacters) {
            logs = logs.substring(logs.length() - maxCharacters);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("namespace", namespace);
        result.put("podName", podName);
        result.put("containerName", containerName);
        result.put("requestedTailLines", 50);
        result.put("logs", logs);

        return result;
    }

    @Tool(
            name = "getKubernetesPods",
            description = "Read Kubernetes pod health, readiness, restarts and termination reasons in an approved namespace"
    )
    public List<Map<String, Object>> getKubernetesPods(
            @ToolParam(description = "Approved Kubernetes namespace")
            String namespace) {

        if (!ALLOWED_NAMESPACES.contains(namespace)) {
            throw new IllegalArgumentException(
                    "Namespace not allowed: " + namespace
            );
        }

        log.info("MCP TOOL EXECUTED: getKubernetesPods({})", namespace);

        List<Pod> pods = client.pods()
                .inNamespace(namespace)
                .list()
                .getItems();

        return pods.stream()
                .limit(50)
                .map(this::toPodHealth)
                .toList();
    }

    private Map<String, Object> toPodHealth(Pod pod) {

        var podStatus = pod.getStatus();

        List<ContainerStatus> statuses =
                podStatus != null && podStatus.getContainerStatuses() != null
                        ? podStatus.getContainerStatuses()
                        : List.of();

        int readyContainers = (int) statuses.stream()
                .filter(status -> Boolean.TRUE.equals(status.getReady()))
                .count();

        int restartCount = statuses.stream()
                .mapToInt(status ->
                        status.getRestartCount() == null
                                ? 0
                                : status.getRestartCount())
                .sum();

        boolean podReady = podStatus != null
                && podStatus.getConditions() != null
                && podStatus.getConditions().stream()
                .anyMatch(condition ->
                        "Ready".equals(condition.getType())
                                && "True".equals(condition.getStatus()));

        List<Map<String, Object>> containers = statuses.stream()
                .map(status -> {

                    Map<String, Object> container = new LinkedHashMap<>();

                    container.put("name", status.getName());
                    container.put("ready", status.getReady());
                    container.put("restartCount", status.getRestartCount());

                    ContainerStateTerminated lastTermination =
                            status.getLastState() != null
                                    ? status.getLastState().getTerminated()
                                    : null;

                    container.put(
                            "lastTerminationReason",
                            lastTermination != null
                                    ? lastTermination.getReason()
                                    : null
                    );

                    container.put(
                            "lastExitCode",
                            lastTermination != null
                                    ? lastTermination.getExitCode()
                                    : null
                    );

                    return container;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("name", pod.getMetadata().getName());
        result.put("namespace", pod.getMetadata().getNamespace());
        result.put("phase",
                podStatus != null ? podStatus.getPhase() : null);
        result.put("podReady", podReady);
        result.put("readyContainers", readyContainers);
        result.put("expectedContainers",
                pod.getSpec() != null && pod.getSpec().getContainers() != null
                        ? pod.getSpec().getContainers().size()
                        : 0);
        result.put("restartCount", restartCount);
        result.put("containers", containers);

        return result;
    }

    @Tool(
            name = "getKubernetesEvents",
            description = "Read recent Kubernetes events in an approved namespace"
    )
    public List<Map<String, Object>> getKubernetesEvents(
            @ToolParam(description = "Approved Kubernetes namespace")
            String namespace) {

        if (!ALLOWED_NAMESPACES.contains(namespace)) {
            throw new IllegalArgumentException(
                    "Namespace not allowed: " + namespace
            );
        }

        log.info("MCP TOOL EXECUTED: getKubernetesEvents({})", namespace);

        List<Event> events = client.v1()
                .events()
                .inNamespace(namespace)
                .list()
                .getItems();

        return events.stream()
                .sorted((a, b) -> {
                    String timeA = a.getLastTimestamp() == null
                            ? ""
                            : a.getLastTimestamp();

                    String timeB = b.getLastTimestamp() == null
                            ? ""
                            : b.getLastTimestamp();

                    return timeB.compareTo(timeA);
                })
                .limit(30)
                .map(event -> {

                    Map<String, Object> result = new LinkedHashMap<>();

                    result.put("type", event.getType());
                    result.put("reason", event.getReason());
                    result.put("message", event.getMessage());
                    result.put("lastTimestamp", event.getLastTimestamp());
                    result.put("count", event.getCount());

                    result.put("involvedObject", event.getInvolvedObject() != null ? event.getInvolvedObject().getName() : null);

                    return result;
                })
                .toList();
    }


    private Map<String, Object> describeProbe(Probe probe) {

        if (probe == null) {
            return Map.of("configured", false);
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("configured", true);

        if (probe.getHttpGet() != null) {
            HTTPGetAction http = probe.getHttpGet();

            details.put("type", "HTTP");
            details.put("path", http.getPath());

            if (http.getPort() != null) {
                details.put(
                        "port",
                        http.getPort().getIntVal() != null
                                ? http.getPort().getIntVal()
                                : http.getPort().getStrVal()
                );
            }
        }

        details.put(
                "initialDelaySeconds",
                probe.getInitialDelaySeconds()
        );
        details.put(
                "periodSeconds",
                probe.getPeriodSeconds()
        );
        details.put(
                "timeoutSeconds",
                probe.getTimeoutSeconds()
        );

        return details;
    }


    private void validateNamespace(String namespace) {

        Set<String> allowedNamespaces = Set.of(
                "default",
                "kube-system",
                "ai-sre-lab"
        );

        if (namespace == null ||
                !allowedNamespaces.contains(namespace)) {

            throw new IllegalArgumentException(
                    "Namespace is not allowed: " + namespace
            );
        }
    }

    private void validatePodName(String podName) {

        if (podName == null ||
                podName.length() > 253 ||
                !podName.matches(
                        "[a-z0-9]([-a-z0-9.]*[a-z0-9])?"
                )) {

            throw new IllegalArgumentException(
                    "Invalid Kubernetes pod name"
            );
        }
    }


}