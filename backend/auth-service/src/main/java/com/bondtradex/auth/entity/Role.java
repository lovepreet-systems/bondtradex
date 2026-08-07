package com.bondtradex.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    protected Role() {
    }

    public Role(UUID id, String name, String description, Set<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public String getDescription() {
        return description;
    }

    public void addPermission(Permission permission) {

        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        permissions.add(permission);
    }

    public void removePermission(Permission permission) {

        if (permission == null) {
            return;
        }

        permissions.remove(permission);
    }

    public void removePermissionByName(String permissionName) {

        if (permissionName == null || permissionName.isBlank()) {
            return;
        }

        permissions.removeIf(permission ->
                permission.getName() != null
                        && permission.getName().equalsIgnoreCase(permissionName.trim()));
    }

    public boolean hasPermission(String permissionName) {

        if (permissionName == null || permissionName.isBlank()) {
            return false;
        }

        return permissions.stream()
                .anyMatch(permission ->
                        permission.getName() != null
                                && permission.getName().equalsIgnoreCase(permissionName.trim()));
    }

    public void changeDescription(String description) {

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }

        this.description = description.trim();
    }
}
