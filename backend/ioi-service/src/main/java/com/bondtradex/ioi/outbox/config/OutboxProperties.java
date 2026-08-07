package com.bondtradex.ioi.outbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox.publisher")
public class OutboxProperties {

    /**
     * Number of events published during one scheduler execution.
     */
    private int batchSize = 100;

    /**
     * Scheduler delay in milliseconds.
     */
    private long fixedDelayMs = 1000;

    /**
     * Maximum retry attempts.
     */
    private int maxRetries = 5;

    /**
     * Retry delay in seconds.
     */
    private int retryDelaySeconds = 30;

    /**
     * Delay between stale PROCESSING recovery executions.
     */
    private long recoveryDelayMs = 60000;

    /**
     * Time after which a PROCESSING event is considered stale.
     */
    private int processingTimeoutSeconds = 120;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getFixedDelayMs() {
        return fixedDelayMs;
    }

    public void setFixedDelayMs(long fixedDelayMs) {
        this.fixedDelayMs = fixedDelayMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(int retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public long getRecoveryDelayMs() {
        return recoveryDelayMs;
    }

    public void setRecoveryDelayMs(long recoveryDelayMs) {
        this.recoveryDelayMs = recoveryDelayMs;
    }

    public int getProcessingTimeoutSeconds() {
        return processingTimeoutSeconds;
    }

    public void setProcessingTimeoutSeconds(
            int processingTimeoutSeconds
    ) {
        this.processingTimeoutSeconds =
                processingTimeoutSeconds;
    }
}