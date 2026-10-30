package com.investhub.api.controller;

import static com.investhub.api.support.SecurityTestHelpers.jwtForUser;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import com.investhub.api.domain.User;
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
class MultiTenantIsolationIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private long userAId;
	private long userBId;
	private long assetOwnedByUserA;

	@BeforeEach
	void seed() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		userRepository.deleteAll();

		User userA = TestUserFactory.persistUser(userRepository, passwordEncoder, "user-a@example.com");
		User userB = TestUserFactory.persistUser(userRepository, passwordEncoder, "user-b@example.com");
		userAId = userA.getId();
		userBId = userB.getId();

		Asset asset = assetRepository.save(new Asset(userA, "VALE3", "Vale", AssetType.STOCK, "BRL"));
		assetOwnedByUserA = asset.getId();
	}

	@Test
	void userBShouldNotAccessAssetOwnedByUserA() throws Exception {
		mockMvc.perform(get("/assets/{id}", assetOwnedByUserA).with(jwtForUser(userBId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)));

		mockMvc.perform(get("/assets").with(jwtForUser(userBId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void sameSymbolCanExistForDifferentUsers() throws Exception {
		mockMvc.perform(post("/assets")
						.with(jwtForUser(userBId))
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{"symbol":"VALE3","name":"Vale ON","assetType":"STOCK","currency":"BRL"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.symbol", is("VALE3")))
				.andExpect(jsonPath("$.id", notNullValue()));

		mockMvc.perform(get("/assets/{id}", assetOwnedByUserA).with(jwtForUser(userAId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.symbol", is("VALE3")));
	}
}
