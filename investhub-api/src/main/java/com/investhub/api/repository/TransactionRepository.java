package com.investhub.api.repository;

import com.investhub.api.domain.Transaction;
import com.investhub.api.domain.TransactionType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Page<Transaction> findByUser_IdAndAsset_Id(Long userId, Long assetId, Pageable pageable);

	List<Transaction> findByUser_IdAndType(Long userId, TransactionType type);
}
