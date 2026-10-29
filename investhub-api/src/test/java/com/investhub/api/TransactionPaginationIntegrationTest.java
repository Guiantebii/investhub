package com.investhub.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoConfigureMockMvc
@Testcontainers
class TransactionPaginationIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	private Long assetId;

	@BeforeEach
	void seedTransactions() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();

		Asset asset = assetRepository.save(new Asset("PETR4", "Petrobras", AssetType.STOCK, "BRL"));
		assetId = asset.getId();

		for (int day = 1; day <= 25; day++) {
			transactionRepository.save(new Transaction(
					asset,
					TransactionType.BUY,
					LocalDate.of(2026, 1, day),
					BigDecimal.TEN,
					BigDecimal.valueOf(day),
					BigDecimal.ZERO,
					null));
		}
	}

	@Test
	void shouldReturnFirstPageWithDefaultSize() throws Exception {
		mockMvc.perform(get("/transactions/asset/{assetId}", assetId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(20)))
				.andExpect(jsonPath("$.page", is(0)))
				.andExpect(jsonPath("$.size", is(20)))
				.andExpect(jsonPath("$.totalElements", is(25)))
				.andExpect(jsonPath("$.totalPages", is(2)))
				.andExpect(jsonPath("$.first", is(true)))
				.andExpect(jsonPath("$.last", is(false)));
	}

	@Test
	void shouldReturnLastPage() throws Exception {
		mockMvc.perform(get("/transactions/asset/{assetId}?page=1&size=20", assetId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(5)))
				.andExpect(jsonPath("$.page", is(1)))
				.andExpect(jsonPath("$.last", is(true)));
	}

	@Test
	void shouldReturn404WhenAssetDoesNotExist() throws Exception {
		mockMvc.perform(get("/transactions/asset/{assetId}", 99999L)).andExpect(status().isNotFound());
	}
}
