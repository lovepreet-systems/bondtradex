package com.bondtradex.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class JwtTokenCustomizerConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            Set<String> authorities = context.getPrincipal()
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toUnmodifiableSet());

            Set<String> roles = authorities.stream()
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .collect(Collectors.toUnmodifiableSet());

            Set<String> permissions = authorities.stream()
                    .filter(authority -> !authority.startsWith("ROLE_"))
                    .collect(Collectors.toUnmodifiableSet());

            context.getClaims()
                    .claim("username", context.getPrincipal().getName())
                    .claim("roles", roles)
                    .claim("permissions", permissions);
        };
    }
}