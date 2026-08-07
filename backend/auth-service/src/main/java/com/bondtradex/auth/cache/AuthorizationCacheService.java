package com.bondtradex.auth.cache;

import com.bondtradex.auth.dto.cache.CachedPermission;
import com.bondtradex.auth.dto.cache.CachedRole;
import com.bondtradex.auth.dto.cache.CachedUser;
import com.bondtradex.auth.dto.cache.CachedUserAuthorization;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthorizationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration cacheTtl;
    private final String keyPrefix;

    public AuthorizationCacheService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${bondtradex.cache.authorization.ttl:7d}")
            Duration cacheTtl,
            @Value("${bondtradex.cache.authorization.prefix:bondtradex:auth:}")
            String keyPrefix
    ) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = cacheTtl;
        this.keyPrefix = keyPrefix;
    }

    public Optional<CachedUser> findUser(String username) {
        String key = AuthorizationCacheKeys.user(
                keyPrefix,
                username
        );

        return read(key, CachedUser.class);
    }

    public void saveUser(CachedUser user) {
        String key = AuthorizationCacheKeys.user(
                keyPrefix,
                user.username()
        );

        write(key, user);
    }

    public Optional<CachedRole> findRole(String roleName) {
        String key = AuthorizationCacheKeys.role(
                keyPrefix,
                roleName
        );

        return read(key, CachedRole.class);
    }

    public void saveRole(CachedRole role) {
        String key = AuthorizationCacheKeys.role(
                keyPrefix,
                role.name()
        );

        write(key, role);
    }

    public Optional<CachedPermission> findPermission(
            String permissionName
    ) {
        String key = AuthorizationCacheKeys.permission(
                keyPrefix,
                permissionName
        );

        return read(key, CachedPermission.class);
    }

    public void savePermission(CachedPermission permission) {
        String key = AuthorizationCacheKeys.permission(
                keyPrefix,
                permission.name()
        );

        write(key, permission);
    }

    public Optional<Set<String>> findUserRoles(UUID userId) {
        String key = AuthorizationCacheKeys.userRoles(
                keyPrefix,
                userId
        );

        return readStringSet(key);
    }

    public void saveUserRoles(
            UUID userId,
            Set<String> roleNames
    ) {
        String key = AuthorizationCacheKeys.userRoles(
                keyPrefix,
                userId
        );

        write(key, Set.copyOf(roleNames));
    }

    public Optional<Set<String>> findRolePermissions(
            UUID roleId
    ) {
        String key = AuthorizationCacheKeys.rolePermissions(
                keyPrefix,
                roleId
        );

        return readStringSet(key);
    }

    public void saveRolePermissions(
            UUID roleId,
            Set<String> permissionNames
    ) {
        String key = AuthorizationCacheKeys.rolePermissions(
                keyPrefix,
                roleId
        );

        write(key, Set.copyOf(permissionNames));
    }

    public Optional<CachedUserAuthorization> findUserAuthorization(
            String username
    ) {
        String key = AuthorizationCacheKeys.userAuthorization(
                keyPrefix,
                username
        );

        return read(key, CachedUserAuthorization.class);
    }

    public void saveUserAuthorization(
            CachedUserAuthorization authorization
    ) {
        String key =
                AuthorizationCacheKeys.userAuthorization(
                        keyPrefix,
                        authorization.username()
                );

        write(key, authorization);
    }

    public void deleteUser(String username) {
        delete(AuthorizationCacheKeys.user(keyPrefix, username));
    }

    public void deleteRole(String roleName) {
        delete(AuthorizationCacheKeys.role(keyPrefix, roleName));
    }

    public void deletePermission(String permissionName) {
        delete(
                AuthorizationCacheKeys.permission(
                        keyPrefix,
                        permissionName
                )
        );
    }

    public void deleteUserRoles(UUID userId) {
        delete(
                AuthorizationCacheKeys.userRoles(
                        keyPrefix,
                        userId
                )
        );
    }

    public void deleteRolePermissions(UUID roleId) {
        delete(
                AuthorizationCacheKeys.rolePermissions(
                        keyPrefix,
                        roleId
                )
        );
    }

    public void deleteUserAuthorization(String username) {
        delete(
                AuthorizationCacheKeys.userAuthorization(
                        keyPrefix,
                        username
                )
        );
    }

    private <T> Optional<T> read(
            String key,
            Class<T> expectedType
    ) {
        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                log.debug("Authorization cache miss. key={}", key);
                return Optional.empty();
            }

            if (!expectedType.isInstance(cachedValue)) {
                log.warn(
                        "Unexpected authorization cache value type. "
                                + "key={}, expected={}, actual={}",
                        key,
                        expectedType.getName(),
                        cachedValue.getClass().getName()
                );

                delete(key);
                return Optional.empty();
            }

            log.debug("Authorization cache hit. key={}", key);

            return Optional.of(expectedType.cast(cachedValue));

        } catch (RedisConnectionFailureException exception) {
            log.warn(
                    "Redis unavailable while reading authorization cache. "
                            + "key={}",
                    key
            );

            return Optional.empty();
        } catch (RuntimeException exception) {
            log.warn(
                    "Unable to read authorization cache. key={}",
                    key,
                    exception
            );

            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<Set<String>> readStringSet(String key) {
        try {
            Object cachedValue =
                    redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                log.debug("Authorization cache miss. key={}", key);
                return Optional.empty();
            }

            if (!(cachedValue instanceof Set<?> rawSet)) {
                log.warn(
                        "Unexpected relationship cache value. key={}",
                        key
                );

                delete(key);
                return Optional.empty();
            }

            boolean containsOnlyStrings = rawSet.stream()
                    .allMatch(String.class::isInstance);

            if (!containsOnlyStrings) {
                log.warn(
                        "Relationship cache contains invalid values. key={}",
                        key
                );

                delete(key);
                return Optional.empty();
            }

            Set<String> values = rawSet.stream()
                    .map(String.class::cast)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());

            log.debug("Authorization cache hit. key={}", key);

            return Optional.of(values);

        } catch (RuntimeException exception) {
            log.warn(
                    "Unable to read relationship cache. key={}",
                    key,
                    exception
            );

            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(
                    key,
                    value,
                    cacheTtl
            );

            log.debug(
                    "Authorization cache entry written. key={}, ttl={}",
                    key,
                    cacheTtl
            );

        } catch (RuntimeException exception) {
            log.warn(
                    "Unable to write authorization cache. key={}",
                    key,
                    exception
            );
        }
    }

    private void delete(String key) {
        try {
            redisTemplate.delete(key);

            log.debug("Authorization cache entry deleted. key={}", key);

        } catch (RuntimeException exception) {
            log.warn(
                    "Unable to delete authorization cache entry. key={}",
                    key,
                    exception
            );
        }
    }
}