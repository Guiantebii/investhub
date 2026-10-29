package com.investhub.api.controller;

import com.investhub.api.dto.request.CreateTransactionRequest;
import com.investhub.api.dto.response.PagedResponse;
import com.investhub.api.dto.response.TransactionResponse;
import com.investhub.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transações", description = "Registro e consulta de aportes e movimentações")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
		TransactionResponse created = transactionService.create(request);
		return ResponseEntity.created(URI.create("/transactions/" + created.id())).body(created);
	}

	@GetMapping("/asset/{assetId}")
	@Operation(
			summary = "Histórico paginado de transações do ativo",
			description = "Parâmetros: page (0-based), size (máx. 100), sort (ex.: occurredAt,desc)")
	public PagedResponse<TransactionResponse> findByAsset(
			@PathVariable Long assetId,
			@ParameterObject
					@PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return transactionService.findByAssetId(assetId, pageable);
	}
}
