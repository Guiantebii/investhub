package com.investhub.api.repository;

import com.investhub.api.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {

	boolean existsBySymbol(String symbol);
}
