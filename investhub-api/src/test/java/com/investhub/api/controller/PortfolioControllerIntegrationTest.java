package com.investhub.api.controller;

import static com.investhub.api.support.SecurityTestHelpers.jwtForUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.domain.User;
import com.investhub.api.integration.brapi.BrapiQuoteService;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import com.investhub.api.repository.UserRepository;
import com.investhub.api.support.TestUserFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class PortfolioControllerIntegrationTest extends AbstractPostgresIntegrationTest {

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

	@MockitoBean
	private BrapiQuoteService brapiQuoteService;

	private long userId;

	@BeforeEach
	void seed() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		userRepository.deleteAll();
		User user = TestUserFactory.persistUser(userRepository, passwordEncoder, "portfolio-test@example.com");
		userId = user.getId();

		Asset asset = assetRepository.save(new Asset(user, "PETR4", "Petrobras", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset,
				TransactionType.BUY,
				LocalDate.of(2026, 1, 10),
				new BigDecimal("10"),
				new BigDecimal("30.0000"),
				BigDecimal.ZERO,
				null));
	}

	@Test
	void shouldExposePortfolioSummaryWithMarkToMarketFields() throws Exception {
		when(brapiQuoteService.getCachedRegularMarketPrice(anyString())).thenReturn(new BigDecimal("40.0000"));

		mockMvc.perform(get("/portfolio/summary").with(jwtForUser(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAllocated").value(300.0))
				.andExpect(jsonPath("$.totalMarketValue").value(400.0))
				.andExpect(jsonPath("$.totalProfitOrLoss").value(100.0))
				.andExpect(jsonPath("$.positions[0].symbol").value("PETR4"))
				.andExpect(jsonPath("$.positions[0].averagePrice").value(30.0))
				.andExpect(jsonPath("$.positions[0].currentPrice").value(40.0));
	}
}
