package com.investhub.api.repository;

import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByAsset_IdOrderByOccurredAtDesc(Long assetId);

	List<Transaction> findByType(TransactionType type);
}
