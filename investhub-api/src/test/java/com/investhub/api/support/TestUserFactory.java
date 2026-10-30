package com.investhub.api.support;

import com.investhub.api.domain.User;
import com.investhub.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class TestUserFactory {

	private TestUserFactory() {}

	public static User persistUser(UserRepository userRepository, PasswordEncoder passwordEncoder, String email) {
		return userRepository.save(new User(email, passwordEncoder.encode("senha-segura")));
	}
}
