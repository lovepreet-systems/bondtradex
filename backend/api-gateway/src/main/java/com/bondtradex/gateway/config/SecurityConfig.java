package com.bondtradex.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .cors(Customizer.withDefaults())

                .authorizeExchange(authorize -> authorize

                        /*
                         * Permit browser CORS preflight requests.
                         */
                        .pathMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /*
                         * Monitoring endpoints used by Docker,
                         * Kubernetes and Prometheus.
                         */
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus"
                        )
                        .permitAll()

                        /*
                         * OAuth2 authorization-server endpoints.
                         *
                         * These must be reachable before the user
                         * possesses an access token.
                         */
                        .pathMatchers(
                                "/oauth2/**",
                                "/login",
                                "/login/**",
                                "/.well-known/**",
                                "/api/auth/health",
                                "/actuator/gateway/**"
                        )
                        .permitAll()

                        /*
                         * Internal circuit-breaker fallback endpoint.
                         */
                        .pathMatchers(
                                "/fallback/**"
                        )
                        .permitAll()

                        /*
                         * Every other route requires a valid JWT.
                         */
                        .anyExchange()
                        .authenticated()
                )

                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(
                                Customizer.withDefaults()
                        )
                )

                .build();
    }
}