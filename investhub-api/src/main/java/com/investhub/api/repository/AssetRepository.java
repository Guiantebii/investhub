package com.investhub.api.repository;

import com.investhub.api.domain.Asset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {

	boolean existsByUser_IdAndSymbol(Long userId, String symbol);

	Optional<Asset> findByIdAndUser_Id(Long id, Long userId);

	List<Asset> findByUser_IdOrderBySymbolAsc(Long userId);

	long countByUser_Id(Long userId);
}
