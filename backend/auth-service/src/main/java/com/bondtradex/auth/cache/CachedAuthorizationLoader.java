package com.bondtradex.auth.cache;

import com.bondtradex.auth.dto.cache.CachedPermission;
import com.bondtradex.auth.dto.cache.CachedRole;
import com.bondtradex.auth.dto.cache.CachedUser;
import com.bondtradex.auth.dto.cache.CachedUserAuthorization;
import com.bondtradex.auth.entity.Permission;
import com.bondtradex.auth.entity.Role;
import com.bondtradex.auth.entity.User;
import com.bondtradex.auth.exception.UserNotFoundException;
import com.bondtradex.auth.repository.UserRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CachedAuthorizationLoader {

    private final UserRepository userRepository;
    private final AuthorizationCacheService cacheService;

    @Transactional(readOnly = true)
    public CachedUserAuthorization load(String username) {
        return cacheService.findUserAuthorization(username)
                .orElseGet(() -> loadFromDatabase(username));
    }

    private CachedUserAuthorization loadFromDatabase(
            String username
    ) {
        User user = userRepository
                .findWithRolesAndPermissionsByUsernameIgnoreCase(
                        username
                )
                .orElseThrow(
                        () -> new UserNotFoundException(username)
                );

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> permissionNames = user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toUnmodifiableSet());

        CachedUserAuthorization authorization =
                new CachedUserAuthorization(
                        user.getId(),
                        user.getUsername(),
                        user.getPasswordHash(),
                        user.isEnabled(),
                        user.isAccountNonExpired(),
                        user.isAccountNonLocked(),
                        user.isCredentialsNonExpired(),
                        roleNames,
                        permissionNames,
                        user.getUpdatedAt()
                );

        populateRelatedCaches(
                user,
                roleNames,
                authorization
        );

        return authorization;
    }

    private void populateRelatedCaches(
            User user,
            Set<String> roleNames,
            CachedUserAuthorization authorization
    ) {
        CachedUser cachedUser = new CachedUser(
                user.getId(),
                user.getUsername(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getUpdatedAt()
        );

        cacheService.saveUser(cachedUser);
        cacheService.saveUserRoles(
                user.getId(),
                roleNames
        );

        for (Role role : user.getRoles()) {
            CachedRole cachedRole = new CachedRole(
                    role.getId(),
                    role.getName(),
                    role.getDescription()
            );

            cacheService.saveRole(cachedRole);

            Set<String> permissionNames =
                    role.getPermissions()
                            .stream()
                            .map(Permission::getName)
                            .collect(
                                    Collectors.toUnmodifiableSet()
                            );

            cacheService.saveRolePermissions(
                    role.getId(),
                    permissionNames
            );

            for (Permission permission : role.getPermissions()) {
                CachedPermission cachedPermission =
                        new CachedPermission(
                                permission.getId(),
                                permission.getName(),
                                permission.getDescription()
                        );

                cacheService.savePermission(cachedPermission);
            }
        }

        cacheService.saveUserAuthorization(authorization);
    }
}