package com.farmtrack.farmtrack.security;

import com.farmtrack.farmtrack.entity.Role;

/**
 * The logged-in user, rebuilt from the JWT on every request.
 * farmerId is null for an ADMIN account.
 */
public record AuthUser(Long userId, Long farmerId, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
