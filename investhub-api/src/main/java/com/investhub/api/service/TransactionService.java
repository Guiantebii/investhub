package com.investhub.api.service;

import com.investhub.api.domain.Asset;
import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import com.investhub.api.dto.request.CreateTransactionRequest;
import com.investhub.api.dto.response.PagedResponse;
import com.investhub.api.dto.response.TransactionResponse;
import com.investhub.api.exception.BusinessValidationException;
import com.investhub.api.exception.InsufficientPositionException;
import com.investhub.api.exception.ResourceNotFoundException;
import com.investhub.api.repository.AssetRepository;
import com.investhub.api.repository.TransactionRepository;
import com.investhub.api.security.SecurityUtils;
import com.investhub.api.service.AssetPositionCalculator.Snapshot;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

	private static final BigDecimal ZERO = BigDecimal.ZERO;

	private final TransactionRepository transactionRepository;
	private final AssetRepository assetRepository;

	public TransactionService(TransactionRepository transactionRepository, AssetRepository assetRepository) {
		this.transactionRepository = transactionRepository;
		this.assetRepository = assetRepository;
	}

	@Transactional
	public TransactionResponse create(CreateTransactionRequest request) {
		Long userId = SecurityUtils.requireCurrentUserId();
		Asset asset = assetRepository
				.findByIdAndUser_Id(request.assetId(), userId)
				.orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado: id=" + request.assetId()));

		validatePositiveAmount(request.quantity(), "quantity");
		validatePositiveAmount(request.unitPrice(), "unitPrice");

		BigDecimal fees = request.fees() != null ? request.fees() : ZERO;
		if (fees.compareTo(ZERO) < 0) {
			throw new BusinessValidationException("fees não pode ser negativo");
		}

		if (request.type() == TransactionType.SELL) {
			validateSellDoesNotExceedPosition(userId, asset, request.quantity());
		}

		Transaction transaction = new Transaction(
				asset,
				request.type(),
				request.occurredAt(),
				request.quantity(),
				request.unitPrice(),
				fees,
				request.notes());

		asset.addTransaction(transaction);
		Transaction saved = transactionRepository.save(transaction);
		return TransactionResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public PagedResponse<TransactionResponse> findByAssetId(Long assetId, Pageable pageable) {
		Long userId = SecurityUtils.requireCurrentUserId();
		if (!assetRepository.findByIdAndUser_Id(assetId, userId).isPresent()) {
			throw new ResourceNotFoundException("Ativo não encontrado: id=" + assetId);
		}
		return PagedResponse.from(transactionRepository
				.findByUser_IdAndAsset_Id(userId, assetId, pageable)
				.map(TransactionResponse::from));
	}

	private void validatePositiveAmount(BigDecimal value, String fieldName) {
		if (value == null || value.compareTo(ZERO) <= 0) {
			throw new BusinessValidationException(fieldName + " deve ser maior que zero");
		}
	}

	private void validateSellDoesNotExceedPosition(Long userId, Asset asset, BigDecimal sellQuantity) {
		List<Transaction> existing =
				transactionRepository.findByUser_IdAndAsset_Id(userId, asset.getId());
		Snapshot snapshot = AssetPositionCalculator.fromTransactions(existing);
		if (sellQuantity.compareTo(snapshot.netQuantity()) > 0) {
			throw new InsufficientPositionException(snapshot.netQuantity(), sellQuantity);
		}
	}
}
