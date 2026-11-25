package org.example.sogone.exception;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
	private String code;
	private String message;
	private Instant restrictedUntil;

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), null);
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.name(), message, null);
	}

	public static ErrorResponse restricted(Instant restrictedUntil) {
		return new ErrorResponse(ErrorCode.USER_RESTRICTED.name(), ErrorCode.USER_RESTRICTED.getMessage(),
			restrictedUntil);
	}
}
