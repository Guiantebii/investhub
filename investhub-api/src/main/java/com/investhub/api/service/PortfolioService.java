package com.investhub.api.service;

import com.investhub.api.domain.Asset;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.dto.response.PortfolioSummaryResponse;
import com.investhub.api.dto.response.PositionSummaryResponse;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
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
	private static final int MONEY_SCALE = 4;
	private static final int QUANTITY_SCALE = 8;

	private final AssetRepository assetRepository;
	private final TransactionRepository transactionRepository;

	public PortfolioService(AssetRepository assetRepository, TransactionRepository transactionRepository) {
		this.assetRepository = assetRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional(readOnly = true)
	public PortfolioSummaryResponse getSummary() {
		List<Asset> assets = assetRepository.findAll();
		List<Transaction> buyTransactions = transactionRepository.findByType(TransactionType.BUY);

		Map<Long, List<Transaction>> buysByAssetId = buyTransactions.stream()
				.collect(Collectors.groupingBy(transaction -> transaction.getAsset().getId()));

		BigDecimal totalAllocated = ZERO;
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

			positions.add(new PositionSummaryResponse(asset.getSymbol(), totalQuantity, totalInvested, averagePrice));
			totalAllocated = totalAllocated.add(totalInvested);
		}

		totalAllocated = totalAllocated.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		return new PortfolioSummaryResponse(totalAllocated, assets.size(), positions);
	}

	private BigDecimal buyTransactionCost(Transaction transaction) {
		return transaction
				.getQuantity()
				.multiply(transaction.getUnitPrice())
				.add(transaction.getFees());
	}
}
