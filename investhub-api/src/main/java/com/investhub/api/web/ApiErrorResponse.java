package com.investhub.api.web;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ApiErrorResponse(Instant timestamp, int status, String error, String message) {

	public static ApiErrorResponse of(HttpStatus httpStatus, String message) {
		return new ApiErrorResponse(Instant.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message);
	}
}
