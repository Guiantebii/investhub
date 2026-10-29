package com.investhub.api.dto.request;

import com.investhub.api.domain.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAssetRequest(
		@NotBlank @Size(max = 32) String symbol,
		@NotBlank @Size(max = 255) String name,
		@NotNull AssetType assetType,
		@NotBlank @Size(min = 3, max = 3) String currency) {}
