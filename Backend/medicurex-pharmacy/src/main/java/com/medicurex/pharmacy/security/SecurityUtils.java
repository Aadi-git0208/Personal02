package com.medicurex.pharmacy.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }

    public static boolean isAdmin() {
        AuthenticatedUser user = currentUserOrNull();
        return user != null && user.isAdmin();
    }
}
