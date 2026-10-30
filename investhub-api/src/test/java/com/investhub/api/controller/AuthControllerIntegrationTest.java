package com.investhub.api.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import com.investhub.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends AbstractPostgresIntegrationTest {

	private static final String PASSWORD = "senha-segura";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	void clean() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void shouldRegisterUserAndNormalizeEmail() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "email": "Investidor@Example.COM",
								  "password": "%s"
								}
								"""
										.formatted(PASSWORD)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.email", is("investidor@example.com")))
				.andExpect(jsonPath("$.createdAt", notNullValue()));
	}

	@Test
	void shouldReturn409WhenEmailAlreadyExists() throws Exception {
		String body =
				"""
				{"email":"duplicado@test.com","password":"%s"}
				"""
						.formatted(PASSWORD);
		mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status", is(409)))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void shouldLoginWithValidCredentials() throws Exception {
		String email = "login@test.com";
		register(email);

		mockMvc.perform(post("/auth/login")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{"email":"%s","password":"%s"}
								"""
										.formatted(email, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.tokenType", is("Bearer")))
				.andExpect(jsonPath("$.expiresIn", is(3600)));
	}

	@Test
	void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
		register("invalid-login@test.com");

		mockMvc.perform(post("/auth/login")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{"email":"invalid-login@test.com","password":"outra-senha"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.message", is("Credenciais inválidas")));
	}

	private void register(String email) throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{"email":"%s","password":"%s"}
								"""
										.formatted(email, PASSWORD)))
				.andExpect(status().isCreated());
	}
}
