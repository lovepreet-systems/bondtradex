package com.bondtradex.auth.dto.cache;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record CachedUser(
        UUID id,
        String username,
        boolean enabled,
        boolean accountNonLocked,
        Instant updatedAt
) implements Serializable {
}
