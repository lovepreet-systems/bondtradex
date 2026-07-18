package com.bondtradex.ioi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .sorted()
                .toList();

        return Map.of(
                "username", authentication.getName(),
                "authenticated", authentication.isAuthenticated(),
                "authorities", authorities
        );
    }
}
