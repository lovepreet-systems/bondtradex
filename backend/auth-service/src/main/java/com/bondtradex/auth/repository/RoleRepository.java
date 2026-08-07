package com.bondtradex.auth.repository;

import com.bondtradex.auth.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findWithPermissionsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @org.springframework.data.jpa.repository.Query("""
        select distinct role.id
        from Role role
        join role.permissions permission
        where lower(permission.name) = lower(:permissionName)
        """)
    java.util.Set<java.util.UUID> findRoleIdsByPermissionName(
            @org.springframework.data.repository.query.Param(
                    "permissionName"
            )
            String permissionName
    );
}
