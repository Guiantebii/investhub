package com.investhub.api.service;

import com.investhub.api.domain.Asset;
import com.investhub.api.domain.User;
import com.investhub.api.dto.request.CreateAssetRequest;
import com.investhub.api.dto.response.AssetResponse;
import com.investhub.api.exception.DuplicateSymbolException;
import com.investhub.api.exception.ResourceNotFoundException;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.UserRepository;
import com.investhub.api.security.SecurityUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

	private final AssetRepository assetRepository;
	private final UserRepository userRepository;

	public AssetService(AssetRepository assetRepository, UserRepository userRepository) {
		this.assetRepository = assetRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public AssetResponse create(CreateAssetRequest request) {
		Long userId = SecurityUtils.requireCurrentUserId();
		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: id=" + userId));

		String symbol = request.symbol().trim().toUpperCase();

		if (assetRepository.existsByUser_IdAndSymbol(userId, symbol)) {
			throw new DuplicateSymbolException(symbol);
		}

		Asset asset = new Asset(user, symbol, request.name().trim(), request.assetType(), request.currency().trim().toUpperCase());
		Asset saved = assetRepository.save(asset);
		return AssetResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<AssetResponse> findAll() {
		Long userId = SecurityUtils.requireCurrentUserId();
		return assetRepository.findByUser_IdOrderBySymbolAsc(userId).stream()
				.map(AssetResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AssetResponse findById(Long id) {
		Long userId = SecurityUtils.requireCurrentUserId();
		Asset asset = assetRepository
				.findByIdAndUser_Id(id, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado: id=" + id));
		return AssetResponse.from(asset);
	}
}
