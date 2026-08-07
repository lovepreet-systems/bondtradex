package com.bondtradex.auth.service;

import com.bondtradex.auth.entity.Role;
import com.bondtradex.auth.entity.User;
import com.bondtradex.auth.event.AuthorizationCacheInvalidationEvent;
import com.bondtradex.auth.event.AuthorizationCacheInvalidationPublisher;
import com.bondtradex.auth.exception.UserNotFoundException;
import com.bondtradex.auth.repository.RoleRepository;
import com.bondtradex.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthorizationCacheInvalidationPublisher cachePublisher;
    private final RoleRepository roleRepository;

    @Transactional
    public void changePassword(
            String username,
            String encodedPassword
    ) {
        User user = userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(
                        () -> new UserNotFoundException(username)
                );

        user.changePassword(encodedPassword);

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forUser(
                        user.getUsername(),
                        user.getId()
                )
        );
    }

    @Transactional
    public void changeEnabledStatus(
            String username,
            boolean enabled
    ) {
        User user = userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(
                        () -> new UserNotFoundException(username)
                );

        user.changeEnabledStatus(enabled);

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forUser(
                        user.getUsername(),
                        user.getId()
                )
        );
    }

    @Transactional
    public void assignRole(
            String username,
            String roleName
    ) throws RoleNotFoundException {
        User user = userRepository
                .findWithRolesAndPermissionsByUsernameIgnoreCase(
                        username
                )
                .orElseThrow(
                        () -> new UserNotFoundException(username)
                );

        Role role = roleRepository
                .findByNameIgnoreCase(roleName)
                .orElseThrow(
                        () -> new RoleNotFoundException(roleName)
                );

        user.addRole(role);

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forUser(
                        user.getUsername(),
                        user.getId()
                )
        );
    }

    @Transactional
    public void removeRole(
            String username,
            String roleName
    ) {
        User user = userRepository
                .findWithRolesAndPermissionsByUsernameIgnoreCase(
                        username
                )
                .orElseThrow(
                        () -> new UserNotFoundException(username)
                );

        user.removeRoleByName(roleName);

        cachePublisher.publish(
                AuthorizationCacheInvalidationEvent.forUser(
                        user.getUsername(),
                        user.getId()
                )
        );
    }
}