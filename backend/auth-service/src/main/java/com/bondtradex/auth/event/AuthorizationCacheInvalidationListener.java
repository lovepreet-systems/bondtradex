package com.bondtradex.auth.event;

import com.bondtradex.auth.cache.AuthorizationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationCacheInvalidationListener {

    private final AuthorizationCacheService cacheService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void invalidate(
            AuthorizationCacheInvalidationEvent event
    ) {
        event.usernames().forEach(username -> {
            cacheService.deleteUser(username);
            cacheService.deleteUserAuthorization(username);
        });

        event.userIds()
                .forEach(cacheService::deleteUserRoles);

        event.roleNames()
                .forEach(cacheService::deleteRole);

        event.roleIds()
                .forEach(cacheService::deleteRolePermissions);

        event.permissionNames()
                .forEach(cacheService::deletePermission);

        log.info(
                "Authorization cache invalidated. "
                        + "users={}, userIds={}, roles={}, "
                        + "roleIds={}, permissions={}",
                event.usernames(),
                event.userIds(),
                event.roleNames(),
                event.roleIds(),
                event.permissionNames()
        );
    }
}