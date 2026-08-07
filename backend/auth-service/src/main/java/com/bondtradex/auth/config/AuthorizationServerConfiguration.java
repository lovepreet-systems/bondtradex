package com.bondtradex.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Configuration
public class AuthorizationServerConfiguration {

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.security.issuer}") String issuer
    ) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }
}