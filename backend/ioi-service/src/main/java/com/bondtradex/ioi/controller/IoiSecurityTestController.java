package com.bondtradex.ioi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/iois")
public class IoiSecurityTestController {

    @GetMapping("/security-test")
    @PreAuthorize("hasAuthority('IOI_CREATE')")
    public Map<String, Object> securityTest(
            Authentication authentication
    ) {
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return Map.of(
                "service", "ioi-service",
                "message", "JWT was validated successfully",
                "username", authentication.getName(),
                "authorities", authorities
        );
    }
}
