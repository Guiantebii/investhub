package com.investhub.api.repository;

import com.investhub.api.domain.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByAsset_IdOrderByOccurredAtDesc(Long assetId);
}
