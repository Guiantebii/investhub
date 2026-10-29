package com.investhub.api.controller;

import com.investhub.api.dto.request.CreateTransactionRequest;
import com.investhub.api.dto.response.TransactionResponse;
import com.investhub.api.service.TransactionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
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
	public List<TransactionResponse> findByAsset(@PathVariable Long assetId) {
		return transactionService.findByAssetId(assetId);
	}
}
