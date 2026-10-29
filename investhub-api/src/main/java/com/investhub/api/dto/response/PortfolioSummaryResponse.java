package com.investhub.api.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryResponse(
		BigDecimal totalAllocated,
		long totalAssetsRegistered,
		BigDecimal totalMarketValue,
		BigDecimal totalProfitOrLoss,
		BigDecimal totalProfitOrLossPercentage,
		List<PositionSummaryResponse> positions) {}
