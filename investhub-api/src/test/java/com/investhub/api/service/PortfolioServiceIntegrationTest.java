package com.investhub.api.service;

import static com.investhub.api.support.SecurityTestHelpers.callAsUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.domain.User;
import com.investhub.api.dto.response.PortfolioSummaryResponse;
import com.investhub.api.dto.response.PositionSummaryResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PortfolioServiceIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private PortfolioService portfolioService;

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
	void clean() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		userRepository.deleteAll();
		userId = TestUserFactory.persistUser(userRepository, passwordEncoder, "portfolio-service@example.com")
				.getId();
		when(brapiQuoteService.getCachedRegularMarketPrice(anyString())).thenReturn(null);
	}

	@Test
	void shouldCalculateAveragePriceFromMultipleBuys() {
		User user = userRepository.findById(userId).orElseThrow();
		Asset asset = assetRepository.save(new Asset(user, "MGLU3", "Magalu", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 1), new BigDecimal("10"), new BigDecimal("20.0000"), new BigDecimal("10.0000"), null));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 2), new BigDecimal("10"), new BigDecimal("40.0000"), new BigDecimal("10.0000"), null));

		PortfolioSummaryResponse summary = callAsUser(userId, () -> portfolioService.getSummary());
		PositionSummaryResponse position = summary.positions().get(0);

		assertThat(position.totalQuantity()).isEqualByComparingTo("20.00000000");
		assertThat(position.totalInvested()).isEqualByComparingTo("620.0000");
		assertThat(position.averagePrice()).isEqualByComparingTo("31.0000");
		assertThat(summary.totalAllocated()).isEqualByComparingTo("620.0000");
	}

	@Test
	void shouldIgnoreNonBuyTransactionsForCostBasis() {
		User user = userRepository.findById(userId).orElseThrow();
		Asset asset = assetRepository.save(new Asset(user, "ABEV3", "Ambev", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 1), new BigDecimal("100"), new BigDecimal("10.0000"), BigDecimal.ZERO, null));
		transactionRepository.save(new Transaction(
				asset, TransactionType.SELL, LocalDate.of(2026, 2, 1), new BigDecimal("40"), new BigDecimal("12.0000"), BigDecimal.ZERO, null));

		PortfolioSummaryResponse summary = callAsUser(userId, () -> portfolioService.getSummary());

		assertThat(summary.positions()).hasSize(1);
		assertThat(summary.positions().get(0).totalQuantity()).isEqualByComparingTo("100.00000000");
		assertThat(summary.positions().get(0).totalInvested()).isEqualByComparingTo("1000.0000");
	}

	@Test
	void shouldCalculateMarkToMarketWhenQuoteIsAvailable() {
		User user = userRepository.findById(userId).orElseThrow();
		Asset asset = assetRepository.save(new Asset(user, "PETR4", "Petrobras", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 10), new BigDecimal("100"), new BigDecimal("30.0000"), BigDecimal.ZERO, null));
		when(brapiQuoteService.getCachedRegularMarketPrice("PETR4")).thenReturn(new BigDecimal("52.4100"));

		PortfolioSummaryResponse summary = callAsUser(userId, () -> portfolioService.getSummary());

		assertThat(summary.totalMarketValue()).isEqualByComparingTo("5241.0000");
		assertThat(summary.totalProfitOrLoss()).isEqualByComparingTo("2241.0000");
		assertThat(summary.positions().get(0).currentPrice()).isEqualByComparingTo("52.4100");
		assertThat(summary.totalProfitOrLossPercentage()).isEqualByComparingTo("74.7000");
	}

	@Test
	void shouldDegradeGracefullyWhenQuoteIsUnavailable() {
		User user = userRepository.findById(userId).orElseThrow();
		Asset asset = assetRepository.save(new Asset(user, "PETR4", "Petrobras", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 10), new BigDecimal("100"), new BigDecimal("30.0000"), BigDecimal.ZERO, null));

		PortfolioSummaryResponse summary = callAsUser(userId, () -> portfolioService.getSummary());

		assertThat(summary.totalMarketValue()).isNull();
		assertThat(summary.totalProfitOrLoss()).isNull();
		assertThat(summary.positions().get(0).currentPrice()).isNull();
		assertThat(summary.totalAllocated()).isEqualByComparingTo("3000.0000");
	}
}
