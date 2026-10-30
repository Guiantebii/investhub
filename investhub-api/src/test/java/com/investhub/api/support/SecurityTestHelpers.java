package com.investhub.api.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.function.Supplier;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class SecurityTestHelpers {

	private SecurityTestHelpers() {}

	public static RequestPostProcessor jwtForUser(long userId) {
		return jwt().jwt(token -> token.subject(String.valueOf(userId)));
	}

	/** @deprecated prefer {@link #jwtForUser(long)} with id real do usuário persistido */
	public static RequestPostProcessor authenticatedJwt() {
		return jwtForUser(1L);
	}

	public static void runAsUser(long userId, Runnable action) {
		callAsUser(userId, () -> {
			action.run();
			return null;
		});
	}

	public static <T> T callAsUser(long userId, Supplier<T> supplier) {
		Jwt jwt = Jwt.withTokenValue("integration-test-token")
				.header("alg", "none")
				.subject(String.valueOf(userId))
				.build();
		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new JwtAuthenticationToken(jwt, AuthorityUtils.createAuthorityList("ROLE_USER")));
		SecurityContextHolder.setContext(context);
		try {
			return supplier.get();
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
}
