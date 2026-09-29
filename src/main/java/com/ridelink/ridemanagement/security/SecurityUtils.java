package com.ridelink.ridemanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility helper to safely access authenticated user identity and authorities.
 */
@Component
public class SecurityUtils {

    /**
     * Resolves the authenticated user's ID from the JWT token.
     * Prioritizes 'sub' standard claim, then 'userId' or 'id'.
     *
     * @return current user identifier, or anonymous if unauthenticated
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            if (jwt.hasClaim("userId")) {
                return jwt.getClaimAsString("userId");
            }
            if (jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
                return jwt.getSubject();
            }
        }

        return authentication.getName();
    }

    /**
     * Retrieves the set of role names associated with the current security context.
     */
    public Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if current user has the specified authority.
     */
    public boolean hasRole(String role) {
        String target = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserRoles().contains(target);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isPassenger() {
        return hasRole("ROLE_PASSENGER");
    }

    public boolean isDriver() {
        return hasRole("ROLE_DRIVER");
    }
}
