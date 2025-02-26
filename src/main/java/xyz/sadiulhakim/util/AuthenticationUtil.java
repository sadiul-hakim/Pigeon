package xyz.sadiulhakim.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import xyz.sadiulhakim.config.security.CustomUserDetails;

import java.util.UUID;

public class AuthenticationUtil {

    public static UUID authenticatedUserId() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            return null;

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return user.id();
    }
}
