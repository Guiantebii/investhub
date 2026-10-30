package com.investhub.api.service;

import com.investhub.api.domain.Asset;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.dto.response.PortfolioSummaryResponse;
import com.investhub.api.dto.response.PositionSummaryResponse;
import com.investhub.api.integration.brapi.BrapiQuoteService;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import com.investhub.api.security.SecurityUtils;
import com.investhub.api.service.AssetPositionCalculator.Snapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final int MONEY_SCALE = 4;
	private static final int PERCENTAGE_SCALE = 4;

	private final AssetRepository assetRepository;
	private final TransactionRepository transactionRepository;
	private final BrapiQuoteService brapiQuoteService;

	public PortfolioService(
			AssetRepository assetRepository,
			TransactionRepository transactionRepository,
			BrapiQuoteService brapiQuoteService) {
		this.assetRepository = assetRepository;
		this.transactionRepository = transactionRepository;
		this.brapiQuoteService = brapiQuoteService;
	}

	@Transactional(readOnly = true)
	public PortfolioSummaryResponse getSummary() {
		Long userId = SecurityUtils.requireCurrentUserId();
		List<Asset> assets = assetRepository.findByUser_IdOrderBySymbolAsc(userId);

		BigDecimal totalAllocated = ZERO;
		BigDecimal totalMarketValue = ZERO;
		BigDecimal totalProfitOrLoss = ZERO;
		BigDecimal totalInvestedWithQuote = ZERO;
		boolean hasAnyMarketValue = false;
		List<PositionSummaryResponse> positions = new ArrayList<>();

		List<Asset> sortedAssets = assets.stream()
				.sorted(Comparator.comparing(Asset::getSymbol))
				.toList();

		for (Asset asset : sortedAssets) {
			List<Transaction> assetTransactions =
					transactionRepository.findByUser_IdAndAsset_Id(userId, asset.getId());
			Snapshot snapshot = AssetPositionCalculator.fromTransactions(assetTransactions);

			if (snapshot.netQuantity().compareTo(ZERO) <= 0) {
				continue;
			}

			MarkToMarket markToMarket =
					calculateMarkToMarket(asset.getSymbol(), snapshot.netQuantity(), snapshot.totalInvested());

			positions.add(new PositionSummaryResponse(
					asset.getSymbol(),
					snapshot.netQuantity(),
					snapshot.totalInvested(),
					snapshot.averagePrice(),
					markToMarket.currentPrice(),
					markToMarket.marketValue(),
					markToMarket.profitOrLoss(),
					markToMarket.profitOrLossPercentage()));

			totalAllocated = totalAllocated.add(snapshot.totalInvested());
			if (markToMarket.marketValue() != null) {
				hasAnyMarketValue = true;
				totalMarketValue = totalMarketValue.add(markToMarket.marketValue());
				totalProfitOrLoss = totalProfitOrLoss.add(markToMarket.profitOrLoss());
				totalInvestedWithQuote = totalInvestedWithQuote.add(snapshot.totalInvested());
			}
		}

		totalAllocated = totalAllocated.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

		BigDecimal portfolioMarketValue = hasAnyMarketValue ? totalMarketValue.setScale(MONEY_SCALE, RoundingMode.HALF_UP) : null;
		BigDecimal portfolioProfitOrLoss = hasAnyMarketValue ? totalProfitOrLoss.setScale(MONEY_SCALE, RoundingMode.HALF_UP) : null;
		BigDecimal portfolioProfitOrLossPercentage = hasAnyMarketValue && totalInvestedWithQuote.compareTo(ZERO) > 0
				? portfolioProfitOrLoss
						.divide(totalInvestedWithQuote, PERCENTAGE_SCALE, RoundingMode.HALF_UP)
						.multiply(ONE_HUNDRED)
				: null;

		return new PortfolioSummaryResponse(
				totalAllocated,
				assetRepository.countByUser_Id(userId),
				portfolioMarketValue,
				portfolioProfitOrLoss,
				portfolioProfitOrLossPercentage,
				positions);
	}

	private MarkToMarket calculateMarkToMarket(String symbol, BigDecimal netQuantity, BigDecimal totalInvested) {
		BigDecimal currentPrice = brapiQuoteService.getCachedRegularMarketPrice(symbol);
		if (currentPrice == null) {
			return new MarkToMarket(null, null, null, null);
		}

		currentPrice = currentPrice.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal marketValue =
				currentPrice.multiply(netQuantity).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal profitOrLoss = marketValue.subtract(totalInvested).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal profitOrLossPercentage = totalInvested.compareTo(ZERO) > 0
				? profitOrLoss
						.divide(totalInvested, PERCENTAGE_SCALE, RoundingMode.HALF_UP)
						.multiply(ONE_HUNDRED)
				: null;

		return new MarkToMarket(currentPrice, marketValue, profitOrLoss, profitOrLossPercentage);
	}

	private record MarkToMarket(
			BigDecimal currentPrice,
			BigDecimal marketValue,
			BigDecimal profitOrLoss,
			BigDecimal profitOrLossPercentage) {}
}
