package com.ridelink.ridemanagement.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Extracts and maps roles from incoming JWT tokens issued by Account Service.
 * Supports various common claim formats (roles, role, authorities, realm_access).
 */
@Component
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Check "roles" claim (List or String)
        Object rolesClaim = jwt.getClaims().get("roles");
        extractRoles(rolesClaim, authorities);

        // 2. Check "role" claim (single String or List)
        Object roleClaim = jwt.getClaims().get("role");
        extractRoles(roleClaim, authorities);

        // 3. Check "authorities" claim
        Object authClaim = jwt.getClaims().get("authorities");
        extractRoles(authClaim, authorities);

        // 4. Check Keycloak style realm_access.roles
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> map) {
            Object realmRoles = map.get("roles");
            extractRoles(realmRoles, authorities);
        }

        // 5. Check "scope" or "scp" claim
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim == null) {
            scopeClaim = jwt.getClaims().get("scp");
        }
        if (scopeClaim instanceof String scopes) {
            for (String s : scopes.split(" ")) {
                if (s.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority(s));
                }
            }
        }

        return authorities.isEmpty() ? Collections.emptyList() : authorities;
    }

    private void extractRoles(Object claim, List<GrantedAuthority> authorities) {
        if (claim instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) {
                    addAuthority(item.toString(), authorities);
                }
            }
        } else if (claim instanceof String stringClaim) {
            addAuthority(stringClaim, authorities);
        }
    }

    private void addAuthority(String roleName, List<GrantedAuthority> authorities) {
        String trimmed = roleName.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String normalized = trimmed.toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(normalized);
        if (!authorities.contains(authority)) {
            authorities.add(authority);
        }
    }
}
