package com.investhub.api.domain;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "assets",
		uniqueConstraints =
				@UniqueConstraint(name = "uk_assets_user_symbol", columnNames = {"user_id", "symbol"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Column(nullable = false, length = 32)
	private String symbol;

	@Column(nullable = false, length = 255)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "asset_type", nullable = false, length = 32)
	private AssetType assetType;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Transaction> transactions = new ArrayList<>();

	public Asset(User user, String symbol, String name, AssetType assetType, String currency) {
		this.user = user;
		this.symbol = symbol;
		this.name = name;
		this.assetType = assetType;
		this.currency = currency;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public void addTransaction(Transaction transaction) {
		transactions.add(transaction);
		transaction.setAsset(this);
	}
}
