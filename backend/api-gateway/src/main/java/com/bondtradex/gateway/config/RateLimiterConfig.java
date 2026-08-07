package com.bondtradex.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver authenticatedUserKeyResolver() {

        return exchange ->
                ReactiveSecurityContextHolder.getContext()
                        .map(securityContext -> securityContext.getAuthentication())
                        .filter(Authentication::isAuthenticated)
                        .map(Authentication::getName)
                        .defaultIfEmpty(
                                exchange.getRequest()
                                        .getRemoteAddress() != null
                                        ? exchange.getRequest()
                                        .getRemoteAddress()
                                        .getAddress()
                                        .getHostAddress()
                                        : "anonymous"
                        );
    }
}