package com.investhub.api.dto.response;

import com.investhub.api.domain.User;
import java.time.Instant;

public record RegisterUserResponse(Long id, String email, Instant createdAt) {

	public static RegisterUserResponse from(User user) {
		return new RegisterUserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
	}
}
