package com.investhub.api.exception;

import java.math.BigDecimal;

public class InsufficientPositionException extends RuntimeException {

	public InsufficientPositionException(BigDecimal availableQuantity, BigDecimal requestedQuantity) {
		super(String.format(
				"Posição insuficiente para venda: quantidade disponível=%s, quantidade solicitada=%s",
				availableQuantity.toPlainString(), requestedQuantity.toPlainString()));
	}
}
