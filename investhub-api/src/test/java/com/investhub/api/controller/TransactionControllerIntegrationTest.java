package com.investhub.api.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class TransactionControllerIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	private Long assetId;

	@BeforeEach
	void seed() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		Asset asset = assetRepository.save(new Asset("BBDC4", "Bradesco", AssetType.STOCK, "BRL"));
		assetId = asset.getId();
	}

	@Test
	void shouldCreateTransaction() throws Exception {
		mockMvc.perform(post("/transactions")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "assetId": %d,
								  "type": "BUY",
								  "occurredAt": "2026-02-01",
								  "quantity": 50,
								  "unitPrice": 14.20,
								  "fees": 5.00
								}
								"""
										.formatted(assetId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.assetId", is(assetId.intValue())))
				.andExpect(jsonPath("$.type", is("BUY")));
	}

	@Test
	void shouldReturn404WhenAssetDoesNotExist() throws Exception {
		mockMvc.perform(post("/transactions")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "assetId": 99999,
								  "type": "BUY",
								  "occurredAt": "2026-02-01",
								  "quantity": 1,
								  "unitPrice": 10.00
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)));
	}

	@Test
	void shouldReturn400WhenQuantityIsNotPositive() throws Exception {
		mockMvc.perform(post("/transactions")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "assetId": %d,
								  "type": "BUY",
								  "occurredAt": "2026-02-01",
								  "quantity": 0,
								  "unitPrice": 10.00
								}
								"""
										.formatted(assetId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)));
	}

	@Test
	void shouldReturn400WhenUnitPriceIsNotPositive() throws Exception {
		mockMvc.perform(post("/transactions")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "assetId": %d,
								  "type": "BUY",
								  "occurredAt": "2026-02-01",
								  "quantity": 10,
								  "unitPrice": -1
								}
								"""
										.formatted(assetId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)));
	}

	@Test
	void shouldReturn400WhenBeanValidationFails() throws Exception {
		mockMvc.perform(post("/transactions")
						.contentType(APPLICATION_JSON)
						.content(
								"""
								{
								  "assetId": %d,
								  "type": "BUY",
								  "quantity": 10,
								  "unitPrice": 10.00
								}
								"""
										.formatted(assetId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)));
	}
}
