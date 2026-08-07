package com.bondtradex.auth.dto.cache;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CachedUserAuthorization(
        UUID userId,
        String username,
        String encodedPassword,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        Set<String> roles,
        Set<String> permissions,
        Instant updatedAt
) implements Serializable {

    public Set<String> authorities() {
        Set<String> authorities = new java.util.HashSet<>();

        authorities.addAll(roles);
        authorities.addAll(permissions);

        return Set.copyOf(authorities);
    }
}
