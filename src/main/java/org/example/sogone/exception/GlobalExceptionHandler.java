package org.example.sogone.exception;

import java.util.ArrayList;
import java.util.List;

import org.example.sogone.global.common.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<ErrorResponse>> handleException(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		log.error("BusinessException : ", e);
		return ResponseEntity.status(code.getStatus())
			.body(ApiResponse.fail(ErrorResponse.of(code), code.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException e) {
		List<String> errors = new ArrayList<>();
		e.getBindingResult().getFieldErrors()
			.forEach(err -> errors.add(err.getDefaultMessage()));

		log.error("MethodArgumentNotValidException : ", e);
		return error(ErrorCode.INVALID_INPUT, errors.toString());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<ErrorResponse>> handleNotReadable(HttpMessageNotReadableException e) {
		log.error("HttpMessageNotReadableException : ", e);
		return error(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.error("MethodArgumentTypeMismatchException : ", e);
		return error(ErrorCode.INVALID_INPUT, e.getName() + ": 형식이 올바르지 않습니다.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<ErrorResponse>> handleUnexpected(Exception e) {
		log.error("Exception", e);
		
		if (e instanceof org.springframework.web.ErrorResponse springError) {
			HttpStatusCode status = springError.getStatusCode();
			return error(ErrorCode.toErrorCode(status));
		}

		ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
		return error(code);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<ErrorResponse>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		log.warn("DataIntegrityViolationException : {}", e.getMostSpecificCause().getMessage());
		ErrorCode code = ErrorCode.DATA_CONFLICT;
		return error(code);
	}

	private ResponseEntity<ApiResponse<ErrorResponse>> error(ErrorCode code) {
		return error(code, code.getMessage());
	}

	private ResponseEntity<ApiResponse<ErrorResponse>> error(ErrorCode code, String message) {
		return ResponseEntity.status(code.getStatus())
			.body(ApiResponse.fail(ErrorResponse.of(code), message));
	}
}
