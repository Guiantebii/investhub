package com.investhub.api.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {

	private SecurityUtils() {}

	public static Long requireCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AuthenticationCredentialsNotFoundException("Usuário não autenticado");
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Jwt jwt) {
			try {
				return Long.parseLong(jwt.getSubject());
			} catch (NumberFormatException ex) {
				throw new AuthenticationCredentialsNotFoundException("Subject JWT inválido");
			}
		}

		throw new AuthenticationCredentialsNotFoundException("Token JWT inválido");
	}
}
