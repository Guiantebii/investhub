package com.investhub.api.controller;

import static com.investhub.api.support.SecurityTestHelpers.jwtForUser;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import com.investhub.api.repository.UserRepository;
import com.investhub.api.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AssetControllerIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private long userId;

	@BeforeEach
	void clean() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		userRepository.deleteAll();
		userId = TestUserFactory.persistUser(userRepository, passwordEncoder, "assets-test@example.com")
				.getId();
	}

	@Test
	void shouldCreateAssetAndNormalizeSymbol() throws Exception {
		mockMvc.perform(post("/assets")
						.with(jwtForUser(userId))
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "symbol": "petr4",
								  "name": "Petrobras PN",
								  "assetType": "STOCK",
								  "currency": "brl"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.symbol", is("PETR4")))
				.andExpect(jsonPath("$.currency", is("BRL")));
	}

	@Test
	void shouldReturn409WhenSymbolAlreadyExists() throws Exception {
		String body =
				"""
				{"symbol":"VALE3","name":"Vale","assetType":"STOCK","currency":"BRL"}
				""";
		mockMvc.perform(post("/assets").with(jwtForUser(userId)).contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/assets").with(jwtForUser(userId)).contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status", is(409)))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void shouldReturn404WhenAssetNotFound() throws Exception {
		mockMvc.perform(get("/assets/{id}", 99999L).with(jwtForUser(userId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)));
	}

	@Test
	void shouldReturn400WhenRequestIsInvalid() throws Exception {
		mockMvc.perform(post("/assets")
						.with(jwtForUser(userId))
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{"symbol":"","name":"X","assetType":"STOCK","currency":"BRL"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)));
	}

	@Test
	void shouldListAssets() throws Exception {
		mockMvc.perform(post("/assets")
						.with(jwtForUser(userId))
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{"symbol":"ITUB4","name":"Itaú","assetType":"STOCK","currency":"BRL"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/assets").with(jwtForUser(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].symbol", is("ITUB4")));
	}
}
