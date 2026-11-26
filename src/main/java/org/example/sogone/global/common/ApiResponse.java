package org.example.sogone.global.common;

import org.example.sogone.exception.ErrorResponse;

public record ApiResponse<T>(
	boolean success,
	String message,
	T data
) {
	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, "success", data);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}

	public static ApiResponse<ErrorResponse> fail(ErrorResponse error, String message) {
		String errorMessage = (!message.isEmpty()) ? message : error.getMessage();
		return new ApiResponse<>(false, errorMessage, error);
	}
}
