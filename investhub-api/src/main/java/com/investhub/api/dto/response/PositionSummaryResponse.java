package com.investhub.api.dto.response;

import java.math.BigDecimal;

public record PositionSummaryResponse(
		String symbol, BigDecimal totalQuantity, BigDecimal totalInvested, BigDecimal averagePrice) {}
