package com.bondtradex.auth.repository;

import com.bondtradex.auth.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);


    Optional<User> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions"
    })
    Optional<User> findWithRolesAndPermissionsByUsernameIgnoreCase(
            String username
    );

    @Query("""
    SELECT DISTINCT u.username
    FROM User u
    JOIN u.roles r
    WHERE LOWER(r.name) = LOWER(:roleName)
    """)
    Set<String> findUsernamesByRoleName(
            @Param("roleName") String roleName
    );

    @org.springframework.data.jpa.repository.Query("""
        select distinct user.username
        from User user
        join user.roles role
        join role.permissions permission
        where lower(permission.name) = lower(:permissionName)
        """)
    java.util.Set<String> findUsernamesByPermissionName(
            @org.springframework.data.repository.query.Param(
                    "permissionName"
            )
            String permissionName
    );

}
