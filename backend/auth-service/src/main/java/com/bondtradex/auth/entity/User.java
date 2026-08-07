package com.bondtradex.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            name = "password_hash",
            nullable = false
    )
    private String passwordHash;

    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = true;

    @Column(
            name = "account_non_expired",
            nullable = false
    )
    private boolean accountNonExpired = true;

    @Column(
            name = "account_non_locked",
            nullable = false
    )
    private boolean accountNonLocked = true;

    @Column(
            name = "credentials_non_expired",
            nullable = false
    )
    private boolean credentialsNonExpired = true;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    nullable = false
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    nullable = false
            )
    )
    private Set<Role> roles = new HashSet<>();

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected User() {
        // Required by JPA.
    }

    public User(
            String username,
            String passwordHash
    ) {
        changeUsername(username);
        changePassword(passwordHash);

        this.enabled = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
    }

    @PrePersist
    private void beforeInsert() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    private void beforeUpdate() {
        updatedAt = Instant.now();
    }

    public void changeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username cannot be null or blank"
            );
        }

        this.username = username.trim();
        touch();
    }

    public void changePassword(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be null or blank"
            );
        }

        this.passwordHash = passwordHash;
        touch();
    }

    public void changeEnabledStatus(boolean enabled) {
        this.enabled = enabled;
        touch();
    }

    public void enable() {
        changeEnabledStatus(true);
    }

    public void disable() {
        changeEnabledStatus(false);
    }

    public void lockAccount() {
        this.accountNonLocked = false;
        touch();
    }

    public void unlockAccount() {
        this.accountNonLocked = true;
        touch();
    }

    public void expireAccount() {
        this.accountNonExpired = false;
        touch();
    }

    public void activateAccount() {
        this.accountNonExpired = true;
        touch();
    }

    public void expireCredentials() {
        this.credentialsNonExpired = false;
        touch();
    }

    public void activateCredentials() {
        this.credentialsNonExpired = true;
        touch();
    }

    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }

        if (roles.add(role)) {
            touch();
        }
    }

    public void removeRole(Role role) {
        if (role == null) {
            return;
        }

        if (roles.remove(role)) {
            touch();
        }
    }

    public void removeRoleByName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return;
        }

        String normalizedRoleName = roleName.trim();

        boolean removed = roles.removeIf(
                role -> role.getName() != null
                        && role.getName().equalsIgnoreCase(
                        normalizedRoleName
                )
        );

        if (removed) {
            touch();
        }
    }

    public boolean hasRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return false;
        }

        String normalizedRoleName = roleName.trim();

        return roles.stream()
                .anyMatch(
                        role -> role.getName() != null
                                && role.getName().equalsIgnoreCase(
                                normalizedRoleName
                        )
                );
    }

    public void clearRoles() {
        if (!roles.isEmpty()) {
            roles.clear();
            touch();
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}