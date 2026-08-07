package com.bondtradex.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter
        implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER =
            "X-Correlation-Id";

    public static final String CORRELATION_ID_ATTRIBUTE =
            CorrelationIdFilter.class.getName()
                    + ".correlationId";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String correlationId =
                resolveCorrelationId(exchange);

        /*
         * Store it in the exchange so that other reactive filters,
         * controllers and exception handlers can access it.
         */
        exchange.getAttributes().put(
                CORRELATION_ID_ATTRIBUTE,
                correlationId
        );

        /*
         * WebFlux request objects are immutable.
         * Mutating creates a new request containing the header.
         */
        ServerHttpRequest updatedRequest =
                exchange.getRequest()
                        .mutate()
                        .headers(headers ->
                                headers.set(
                                        CORRELATION_ID_HEADER,
                                        correlationId
                                )
                        )
                        .build();

        ServerWebExchange updatedExchange =
                exchange.mutate()
                        .request(updatedRequest)
                        .build();

        /*
         * Add the correlation ID to the response before the response
         * is committed. This lets clients report the exact request ID
         * when troubleshooting.
         */
        updatedExchange.getResponse()
                .beforeCommit(() -> {
                    updatedExchange.getResponse()
                            .getHeaders()
                            .set(
                                    CORRELATION_ID_HEADER,
                                    correlationId
                            );

                    return Mono.empty();
                });

        return chain.filter(updatedExchange)
                .contextWrite(context ->
                        context.put(
                                CORRELATION_ID_ATTRIBUTE,
                                correlationId
                        )
                );
    }

    private String resolveCorrelationId(
            ServerWebExchange exchange
    ) {
        String suppliedCorrelationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                CORRELATION_ID_HEADER
                        );

        if (suppliedCorrelationId == null
                || suppliedCorrelationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return suppliedCorrelationId.trim();
    }

    @Override
    public int getOrder() {
        /*
         * Run near the beginning of the pre-filter phase so that
         * subsequent gateway filters can access the correlation ID.
         *
         * Avoid Integer.MIN_VALUE because Spring's own infrastructure
         * filters may also use extreme precedence values.
         */
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}