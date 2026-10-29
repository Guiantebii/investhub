package com.investhub.api.exception;

public class DuplicateSymbolException extends RuntimeException {

	public DuplicateSymbolException(String symbol) {
		super("Já existe um ativo cadastrado com o symbol: " + symbol);
	}
}
