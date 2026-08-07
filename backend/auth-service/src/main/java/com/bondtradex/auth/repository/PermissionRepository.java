package com.bondtradex.auth.repository;

import com.bondtradex.auth.entity.Permission;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("""
            SELECT DISTINCT r.id
            FROM Role r
            JOIN r.permissions p
            WHERE LOWER(p.name) = LOWER(:permissionName)
            """)
    Set<UUID> findRoleIdsByPermissionName(
            @Param("permissionName") String permissionName
    );

    @Query("""
            SELECT DISTINCT u.username
            FROM User u
            JOIN u.roles r
            JOIN r.permissions p
            WHERE LOWER(p.name) = LOWER(:permissionName)
            """)
    Set<String> findUsernamesByPermissionName(
            @Param("permissionName") String permissionName
    );
}