package com.bondtradex.ioi.config;

import com.bondtradex.ioi.outbox.config.OutboxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxConfig {

    @Bean
    public Clock outboxClock() {
        return Clock.systemUTC();
    }
}

