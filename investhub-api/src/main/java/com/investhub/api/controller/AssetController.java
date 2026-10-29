package com.investhub.api.controller;

import com.investhub.api.dto.request.CreateAssetRequest;
import com.investhub.api.dto.response.AssetResponse;
import com.investhub.api.service.AssetService;
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
@RequestMapping("/assets")
public class AssetController {

	private final AssetService assetService;

	public AssetController(AssetService assetService) {
		this.assetService = assetService;
	}

	@PostMapping
	public ResponseEntity<AssetResponse> create(@Valid @RequestBody CreateAssetRequest request) {
		AssetResponse created = assetService.create(request);
		return ResponseEntity.created(URI.create("/assets/" + created.id())).body(created);
	}

	@GetMapping
	public List<AssetResponse> findAll() {
		return assetService.findAll();
	}

	@GetMapping("/{id}")
	public AssetResponse findById(@PathVariable Long id) {
		return assetService.findById(id);
	}
}
