package com.bondtradex.auth.service;

import com.bondtradex.auth.entity.Permission;
import com.bondtradex.auth.event.AuthorizationCacheInvalidationEvent;
import com.bondtradex.auth.event.AuthorizationCacheInvalidationPublisher;
import com.bondtradex.auth.exception.PermissionNotFoundException;
import com.bondtradex.auth.repository.PermissionRepository;
import com.bondtradex.auth.repository.RoleRepository;
import com.bondtradex.auth.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthorizationCacheInvalidationPublisher cachePublisher;

    @Transactional
    public void updateDescription(
            String permissionName,
            String description
    ) {
        Permission permission = permissionRepository
                .findByNameIgnoreCase(permissionName)
                .orElseThrow(
                        () -> new PermissionNotFoundException(
                                permissionName
                        )
                );

        permission.changeDescription(description);

        Set<UUID> affectedRoleIds =
                roleRepository.findRoleIdsByPermissionName(
                        permission.getName()
                );

        Set<String> affectedUsernames =
                userRepository.findUsernamesByPermissionName(
                        permission.getName()
                );

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forPermission(
                        permission.getName(),
                        affectedRoleIds,
                        affectedUsernames
                )
        );
    }
}