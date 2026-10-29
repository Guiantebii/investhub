package com.investhub.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.investhub.api.AbstractPostgresIntegrationTest;
import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.dto.response.PortfolioSummaryResponse;
import com.investhub.api.dto.response.PositionSummaryResponse;
import com.investhub.api.integration.brapi.BrapiQuoteService;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PortfolioServiceIntegrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@MockitoBean
	private BrapiQuoteService brapiQuoteService;

	@BeforeEach
	void clean() {
		transactionRepository.deleteAll();
		assetRepository.deleteAll();
		when(brapiQuoteService.getCachedRegularMarketPrice(anyString())).thenReturn(null);
	}

	@Test
	void shouldCalculateAveragePriceFromMultipleBuys() {
		Asset asset = assetRepository.save(new Asset("MGLU3", "Magalu", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 1), new BigDecimal("10"), new BigDecimal("20.0000"), new BigDecimal("10.0000"), null));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 2), new BigDecimal("10"), new BigDecimal("40.0000"), new BigDecimal("10.0000"), null));

		PortfolioSummaryResponse summary = portfolioService.getSummary();
		PositionSummaryResponse position = summary.positions().get(0);

		assertThat(position.totalQuantity()).isEqualByComparingTo("20.00000000");
		assertThat(position.totalInvested()).isEqualByComparingTo("620.0000");
		assertThat(position.averagePrice()).isEqualByComparingTo("31.0000");
		assertThat(summary.totalAllocated()).isEqualByComparingTo("620.0000");
	}

	@Test
	void shouldIgnoreNonBuyTransactionsForCostBasis() {
		Asset asset = assetRepository.save(new Asset("ABEV3", "Ambev", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 1), new BigDecimal("100"), new BigDecimal("10.0000"), BigDecimal.ZERO, null));
		transactionRepository.save(new Transaction(
				asset, TransactionType.SELL, LocalDate.of(2026, 2, 1), new BigDecimal("40"), new BigDecimal("12.0000"), BigDecimal.ZERO, null));

		PortfolioSummaryResponse summary = portfolioService.getSummary();

		assertThat(summary.positions()).hasSize(1);
		assertThat(summary.positions().get(0).totalQuantity()).isEqualByComparingTo("100.00000000");
		assertThat(summary.positions().get(0).totalInvested()).isEqualByComparingTo("1000.0000");
	}

	@Test
	void shouldCalculateMarkToMarketWhenQuoteIsAvailable() {
		Asset asset = assetRepository.save(new Asset("PETR4", "Petrobras", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 10), new BigDecimal("100"), new BigDecimal("30.0000"), BigDecimal.ZERO, null));
		when(brapiQuoteService.getCachedRegularMarketPrice("PETR4")).thenReturn(new BigDecimal("52.4100"));

		PortfolioSummaryResponse summary = portfolioService.getSummary();

		assertThat(summary.totalMarketValue()).isEqualByComparingTo("5241.0000");
		assertThat(summary.totalProfitOrLoss()).isEqualByComparingTo("2241.0000");
		assertThat(summary.positions().get(0).currentPrice()).isEqualByComparingTo("52.4100");
		assertThat(summary.totalProfitOrLossPercentage()).isEqualByComparingTo("74.7000");
	}

	@Test
	void shouldDegradeGracefullyWhenQuoteIsUnavailable() {
		Asset asset = assetRepository.save(new Asset("PETR4", "Petrobras", AssetType.STOCK, "BRL"));
		transactionRepository.save(new Transaction(
				asset, TransactionType.BUY, LocalDate.of(2026, 1, 10), new BigDecimal("100"), new BigDecimal("30.0000"), BigDecimal.ZERO, null));

		PortfolioSummaryResponse summary = portfolioService.getSummary();

		assertThat(summary.totalMarketValue()).isNull();
		assertThat(summary.totalProfitOrLoss()).isNull();
		assertThat(summary.positions().get(0).currentPrice()).isNull();
		assertThat(summary.totalAllocated()).isEqualByComparingTo("3000.0000");
	}
}
