package com.bondtradex.auth.event;

import java.util.Set;
import java.util.UUID;

public record AuthorizationCacheInvalidationEvent(
        Set<String> usernames,
        Set<UUID> userIds,
        Set<String> roleNames,
        Set<UUID> roleIds,
        Set<String> permissionNames
) {

    public static AuthorizationCacheInvalidationEvent forUser(
            String username,
            UUID userId
    ) {
        return new AuthorizationCacheInvalidationEvent(
                Set.of(username),
                Set.of(userId),
                Set.of(),
                Set.of(),
                Set.of()
        );
    }

    public static AuthorizationCacheInvalidationEvent forRole(
            String roleName,
            UUID roleId,
            Set<String> affectedUsernames
    ) {
        return new AuthorizationCacheInvalidationEvent(
                Set.copyOf(affectedUsernames),
                Set.of(),
                Set.of(roleName),
                Set.of(roleId),
                Set.of()
        );
    }

    public static AuthorizationCacheInvalidationEvent forPermission(
            String permissionName,
            Set<UUID> affectedRoleIds,
            Set<String> affectedUsernames
    ) {
        return new AuthorizationCacheInvalidationEvent(
                Set.copyOf(affectedUsernames),
                Set.of(),
                Set.of(),
                Set.copyOf(affectedRoleIds),
                Set.of(permissionName)
        );
    }
}