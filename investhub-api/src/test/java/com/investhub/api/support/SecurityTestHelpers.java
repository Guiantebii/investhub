package com.investhub.api.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class SecurityTestHelpers {

	private SecurityTestHelpers() {}

	public static RequestPostProcessor authenticatedJwt() {
		return jwt().jwt(token -> token.subject("1"));
	}
}
