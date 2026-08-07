package com.bondtradex.ioi.downstream.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IoiDownstreamProcessingService {

    public void process(String payload) {

        log.info("Processing event {}", payload);

        /*
         * Simulate failures.
         */

        if (payload.contains("FAIL")) {
            throw new RuntimeException(
                    "Simulated downstream failure"
            );
        }

        log.info("Processing completed.");
    }
}