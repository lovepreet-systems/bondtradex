package com.bondtradex.auth.service;

import com.bondtradex.auth.entity.Permission;
import com.bondtradex.auth.entity.Role;
import com.bondtradex.auth.event.AuthorizationCacheInvalidationEvent;
import com.bondtradex.auth.event.AuthorizationCacheInvalidationPublisher;
import com.bondtradex.auth.exception.PermissionNotFoundException;
import com.bondtradex.auth.exception.RoleNotFoundException;
import com.bondtradex.auth.repository.PermissionRepository;
import com.bondtradex.auth.repository.RoleRepository;
import com.bondtradex.auth.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthorizationCacheInvalidationPublisher cachePublisher;
    private final PermissionRepository permissionRepository;

    @Transactional
    public void updateDescription(
            String roleName,
            String description
    ) {
        Role role = roleRepository
                .findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));
        role.changeDescription(description);

        Set<String> affectedUsernames =
                userRepository.findUsernamesByRoleName(
                        role.getName()
                );

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forRole(
                        role.getName(),
                        role.getId(),
                        affectedUsernames
                )
        );
    }

    @Transactional
    public void assignPermission(
            String roleName,
            String permissionName
    ) {
        Role role = roleRepository
                .findWithPermissionsByNameIgnoreCase(roleName)
                .orElseThrow(
                        () -> new RoleNotFoundException(roleName)
                );

        Permission permission = permissionRepository
                .findByNameIgnoreCase(permissionName)
                .orElseThrow(
                        () -> new PermissionNotFoundException(
                                permissionName
                        )
                );

        role.addPermission(permission);

        Set<String> affectedUsernames =
                userRepository.findUsernamesByRoleName(
                        role.getName()
                );

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forRole(
                        role.getName(),
                        role.getId(),
                        affectedUsernames
                )
        );
    }
}