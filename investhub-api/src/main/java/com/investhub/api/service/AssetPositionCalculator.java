package com.investhub.api.service;

import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Posição líquida e preço médio (custo das compras ÷ quantidade comprada).
 * Vendas reduzem apenas a quantidade líquida; o preço médio não muda com SELL.
 */
public final class AssetPositionCalculator {

	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final int MONEY_SCALE = 4;
	private static final int QUANTITY_SCALE = 8;

	private AssetPositionCalculator() {}

	public record Snapshot(
			BigDecimal boughtQuantity,
			BigDecimal soldQuantity,
			BigDecimal netQuantity,
			BigDecimal averagePrice,
			BigDecimal totalInvested) {}

	public static Snapshot fromTransactions(List<Transaction> transactions) {
		BigDecimal boughtQuantity = ZERO;
		BigDecimal soldQuantity = ZERO;
		BigDecimal totalBuyCost = ZERO;

		for (Transaction transaction : transactions) {
			if (transaction.getType() == TransactionType.BUY) {
				boughtQuantity = boughtQuantity.add(transaction.getQuantity());
				totalBuyCost = totalBuyCost.add(buyTransactionCost(transaction));
			} else if (transaction.getType() == TransactionType.SELL) {
				soldQuantity = soldQuantity.add(transaction.getQuantity());
			}
		}

		BigDecimal averagePrice = boughtQuantity.compareTo(ZERO) > 0
				? totalBuyCost.divide(boughtQuantity, MONEY_SCALE, RoundingMode.HALF_UP)
				: ZERO;

		BigDecimal netQuantity = boughtQuantity.subtract(soldQuantity);
		if (netQuantity.compareTo(ZERO) < 0) {
			netQuantity = ZERO;
		}

		BigDecimal totalInvested = netQuantity.compareTo(ZERO) > 0
				? netQuantity.multiply(averagePrice).setScale(MONEY_SCALE, RoundingMode.HALF_UP)
				: ZERO;

		return new Snapshot(
				boughtQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP),
				soldQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP),
				netQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP),
				averagePrice,
				totalInvested);
	}

	private static BigDecimal buyTransactionCost(Transaction transaction) {
		return transaction
				.getQuantity()
				.multiply(transaction.getUnitPrice())
				.add(transaction.getFees());
	}
}
