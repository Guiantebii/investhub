package com.investhub.api.dto.response;

import com.investhub.api.domain.Asset;
import com.investhub.api.domain.AssetType;
import java.time.Instant;

public record AssetResponse(
		Long id, String symbol, String name, AssetType assetType, String currency, Instant createdAt) {

	public static AssetResponse from(Asset asset) {
		return new AssetResponse(
				asset.getId(),
				asset.getSymbol(),
				asset.getName(),
				asset.getAssetType(),
				asset.getCurrency(),
				asset.getCreatedAt());
	}
}
