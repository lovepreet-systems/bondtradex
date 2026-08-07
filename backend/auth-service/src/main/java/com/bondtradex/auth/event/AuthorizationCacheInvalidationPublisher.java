package com.bondtradex.auth.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationCacheInvalidationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(AuthorizationCacheInvalidationEvent event) {
        eventPublisher.publishEvent(event);
    }
}