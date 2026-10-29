package com.investhub.api.dto.request;

import com.investhub.api.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
		@NotNull Long assetId,
		@NotNull TransactionType type,
		@NotNull LocalDate occurredAt,
		@NotNull BigDecimal quantity,
		@NotNull BigDecimal unitPrice,
		BigDecimal fees,
		@Size(max = 500) String notes) {}
