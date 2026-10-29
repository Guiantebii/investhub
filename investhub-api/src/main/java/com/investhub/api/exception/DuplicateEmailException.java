package com.investhub.api.exception;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String email) {
		super("Já existe um usuário cadastrado com o e-mail: " + email);
	}
}
