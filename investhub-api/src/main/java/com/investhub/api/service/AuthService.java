package com.investhub.api.service;

import com.investhub.api.config.JwtProperties;
import com.investhub.api.domain.User;
import com.investhub.api.dto.request.LoginRequest;
import com.investhub.api.dto.request.RegisterUserRequest;
import com.investhub.api.dto.response.AuthTokenResponse;
import com.investhub.api.dto.response.RegisterUserResponse;
import com.investhub.api.exception.DuplicateEmailException;
import com.investhub.api.repository.UserRepository;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager,
			JwtEncoder jwtEncoder,
			JwtProperties jwtProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = jwtProperties;
	}

	@Transactional
	public RegisterUserResponse register(RegisterUserRequest request) {
		String email = request.email().trim().toLowerCase();

		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateEmailException(email);
		}

		String passwordHash = passwordEncoder.encode(request.password());
		User user = new User(email, passwordHash);
		User saved = userRepository.save(user);
		return RegisterUserResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public AuthTokenResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

		User user = userRepository
				.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + email));

		Instant now = Instant.now();
		Instant expiresAt = now.plus(jwtProperties.accessTokenTtl());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.issuedAt(now)
				.expiresAt(expiresAt)
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String accessToken =
				jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		long expiresInSeconds = jwtProperties.accessTokenTtl().toSeconds();
		return new AuthTokenResponse(accessToken, "Bearer", expiresInSeconds);
	}
}
