package com.farmtrack.farmtrack.security;

import com.farmtrack.farmtrack.exception.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Ownership rules in one place: a FARMER may only touch their own farmer record, crops,
 * expenses and incomes; an ADMIN may touch everything.
 */
@Service
public class CurrentUserService {

    public AuthUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new AuthenticationCredentialsNotFoundException("Not logged in");
        }
        return authUser;
    }

    /** Throws 403 unless the current user is that farmer or an admin. */
    public void checkFarmerAccess(Long farmerId) {
        AuthUser user = get();
        if (!user.isAdmin() && !Objects.equals(user.farmerId(), farmerId)) {
            throw new AccessDeniedException("Access denied to farmer " + farmerId);
        }
    }

    /**
     * Which farmer's data to show. Farmers always get their own data.
     * Admins must say which farmer they want with ?farmerId=.
     */
    public Long resolveFarmerId(Long requestedFarmerId) {
        AuthUser user = get();
        if (!user.isAdmin()) {
            if (requestedFarmerId != null && !requestedFarmerId.equals(user.farmerId())) {
                throw new AccessDeniedException("Access denied to farmer " + requestedFarmerId);
            }
            return user.farmerId();
        }
        if (requestedFarmerId == null) {
            throw new BadRequestException("farmerId query parameter is required for admin users");
        }
        return requestedFarmerId;
    }
}
