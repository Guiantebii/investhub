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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final int MONEY_SCALE = 4;
	private static final int PERCENTAGE_SCALE = 4;
	private static final int QUANTITY_SCALE = 8;

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
		List<Transaction> buyTransactions = transactionRepository.findByUser_IdAndType(userId, TransactionType.BUY);

		Map<Long, List<Transaction>> buysByAssetId = buyTransactions.stream()
				.collect(Collectors.groupingBy(transaction -> transaction.getAsset().getId()));

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
			List<Transaction> assetBuys = buysByAssetId.getOrDefault(asset.getId(), List.of());

			BigDecimal totalQuantity = assetBuys.stream()
					.map(Transaction::getQuantity)
					.reduce(ZERO, BigDecimal::add);

			if (totalQuantity.compareTo(ZERO) <= 0) {
				continue;
			}

			BigDecimal totalInvested = assetBuys.stream()
					.map(this::buyTransactionCost)
					.reduce(ZERO, BigDecimal::add);

			BigDecimal averagePrice = totalInvested.divide(totalQuantity, MONEY_SCALE, RoundingMode.HALF_UP);

			totalQuantity = totalQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
			totalInvested = totalInvested.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

			MarkToMarket markToMarket = calculateMarkToMarket(asset.getSymbol(), totalQuantity, totalInvested);

			positions.add(new PositionSummaryResponse(
					asset.getSymbol(),
					totalQuantity,
					totalInvested,
					averagePrice,
					markToMarket.currentPrice(),
					markToMarket.marketValue(),
					markToMarket.profitOrLoss(),
					markToMarket.profitOrLossPercentage()));

			totalAllocated = totalAllocated.add(totalInvested);
			if (markToMarket.marketValue() != null) {
				hasAnyMarketValue = true;
				totalMarketValue = totalMarketValue.add(markToMarket.marketValue());
				totalProfitOrLoss = totalProfitOrLoss.add(markToMarket.profitOrLoss());
				totalInvestedWithQuote = totalInvestedWithQuote.add(totalInvested);
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

	private MarkToMarket calculateMarkToMarket(String symbol, BigDecimal totalQuantity, BigDecimal totalInvested) {
		BigDecimal currentPrice = brapiQuoteService.getCachedRegularMarketPrice(symbol);
		if (currentPrice == null) {
			return new MarkToMarket(null, null, null, null);
		}

		currentPrice = currentPrice.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal marketValue =
				currentPrice.multiply(totalQuantity).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal profitOrLoss = marketValue.subtract(totalInvested).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal profitOrLossPercentage = totalInvested.compareTo(ZERO) > 0
				? profitOrLoss
						.divide(totalInvested, PERCENTAGE_SCALE, RoundingMode.HALF_UP)
						.multiply(ONE_HUNDRED)
				: null;

		return new MarkToMarket(currentPrice, marketValue, profitOrLoss, profitOrLossPercentage);
	}

	private BigDecimal buyTransactionCost(Transaction transaction) {
		return transaction
				.getQuantity()
				.multiply(transaction.getUnitPrice())
				.add(transaction.getFees());
	}

	private record MarkToMarket(
			BigDecimal currentPrice,
			BigDecimal marketValue,
			BigDecimal profitOrLoss,
			BigDecimal profitOrLossPercentage) {}
}
