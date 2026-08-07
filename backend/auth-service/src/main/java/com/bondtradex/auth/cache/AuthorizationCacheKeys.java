package com.bondtradex.auth.cache;

import java.util.Locale;
import java.util.UUID;

public final class AuthorizationCacheKeys {

    private static final String USER = "user:";
    private static final String ROLE = "role:";
    private static final String PERMISSION = "permission:";
    private static final String USER_ROLES = "user-roles:";
    private static final String ROLE_PERMISSIONS = "role-permissions:";
    private static final String USER_AUTHORIZATION = "user-authorization:";

    private AuthorizationCacheKeys() {
    }

    public static String user(String prefix, String username) {
        return prefix + USER + normalize(username);
    }

    public static String role(String prefix, String roleName) {
        return prefix + ROLE + normalize(roleName);
    }

    public static String permission(
            String prefix,
            String permissionName
    ) {
        return prefix + PERMISSION + normalize(permissionName);
    }

    public static String userRoles(String prefix, UUID userId) {
        return prefix + USER_ROLES + userId;
    }

    public static String rolePermissions(String prefix, UUID roleId) {
        return prefix + ROLE_PERMISSIONS + roleId;
    }

    public static String userAuthorization(
            String prefix,
            String username
    ) {
        return prefix + USER_AUTHORIZATION + normalize(username);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}