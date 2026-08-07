package com.bondtradex.gateway.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(
            value = "/ioi",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Map<String, Object>> ioiFallback() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service temporarily unavailable");
        response.put("service", "ioi-service");
        response.put(
                "message",
                "The IOI service is currently unavailable."
        );

        return Mono.just(response);
    }
}