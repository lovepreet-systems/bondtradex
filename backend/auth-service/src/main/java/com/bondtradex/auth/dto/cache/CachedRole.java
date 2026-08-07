package com.bondtradex.auth.dto.cache;

import java.io.Serializable;
import java.util.UUID;

public record CachedRole(
        UUID id,
        String name,
        String description
) implements Serializable {
}
