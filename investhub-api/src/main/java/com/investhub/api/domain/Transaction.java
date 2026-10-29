package com.investhub.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "asset_id", nullable = false)
	private Asset asset;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TransactionType type;

	@Column(name = "occurred_at", nullable = false)
	private LocalDate occurredAt;

	@Column(nullable = false, precision = 19, scale = 8)
	private BigDecimal quantity;

	@Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal unitPrice;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal fees;

	@Column(length = 500)
	private String notes;

	public Transaction(
			Asset asset,
			TransactionType type,
			LocalDate occurredAt,
			BigDecimal quantity,
			BigDecimal unitPrice,
			BigDecimal fees,
			String notes) {
		this.asset = asset;
		this.type = type;
		this.occurredAt = occurredAt;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.fees = fees;
		this.notes = notes;
	}
}
