package com.investhub.api.dto.response;

import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
		Long id,
		Long assetId,
		TransactionType type,
		LocalDate occurredAt,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal fees,
		String notes) {

	public static TransactionResponse from(Transaction transaction) {
		return new TransactionResponse(
				transaction.getId(),
				transaction.getAsset().getId(),
				transaction.getType(),
				transaction.getOccurredAt(),
				transaction.getQuantity(),
				transaction.getUnitPrice(),
				transaction.getFees(),
				transaction.getNotes());
	}
}
