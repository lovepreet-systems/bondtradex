package com.bondtradex.ioi.controller;

import com.bondtradex.ioi.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/auth/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health(){
        Map<String, String> data = Map.of(
                "service", "auth-service",
                "status", "UP"
        );
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running", data)
        );
    }
}
