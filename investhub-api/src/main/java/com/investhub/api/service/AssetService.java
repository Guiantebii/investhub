package com.investhub.api.service;

import com.investhub.api.domain.Asset;
import com.investhub.api.dto.request.CreateAssetRequest;
import com.investhub.api.dto.response.AssetResponse;
import com.investhub.api.exception.DuplicateSymbolException;
import com.investhub.api.repository.AssetRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

	private final AssetRepository assetRepository;

	public AssetService(AssetRepository assetRepository) {
		this.assetRepository = assetRepository;
	}

	@Transactional
	public AssetResponse create(CreateAssetRequest request) {
		String symbol = request.symbol().trim().toUpperCase();

		if (assetRepository.existsBySymbol(symbol)) {
			throw new DuplicateSymbolException(symbol);
		}

		Asset asset = new Asset(symbol, request.name().trim(), request.assetType(), request.currency().trim().toUpperCase());
		Asset saved = assetRepository.save(asset);
		return AssetResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<AssetResponse> findAll() {
		return assetRepository.findAll().stream().map(AssetResponse::from).toList();
	}
}
