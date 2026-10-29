package com.investhub.api.repository;

import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Page<Transaction> findByAsset_Id(Long assetId, Pageable pageable);

	List<Transaction> findByType(TransactionType type);
}
