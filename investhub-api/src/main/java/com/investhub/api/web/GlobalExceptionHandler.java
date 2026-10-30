package com.investhub.api.web;

import com.investhub.api.exception.BusinessValidationException;
import com.investhub.api.exception.DuplicateEmailException;
import com.investhub.api.exception.DuplicateSymbolException;
import com.investhub.api.exception.InsufficientPositionException;
import com.investhub.api.exception.ResourceNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage()));
	}

	@ExceptionHandler(DuplicateSymbolException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateSymbol(DuplicateSymbolException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
	}

	@ExceptionHandler(BusinessValidationException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessValidation(BusinessValidationException ex) {
		return ResponseEntity.badRequest().body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(InsufficientPositionException.class)
	public ResponseEntity<ApiErrorResponse> handleInsufficientPosition(InsufficientPositionException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ApiErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST, detail));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
	}
}
