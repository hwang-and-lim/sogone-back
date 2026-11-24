package org.example.sogone.exception;

import java.util.Arrays;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleException(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		String message = (fieldError != null)
			? fieldError.getField() + ": " + fieldError.getDefaultMessage()
			: ErrorCode.INVALID_INPUT.getMessage();
		return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		if (e.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
			String field = ife.getPath().isEmpty() ? "요청 값" : ife.getPath().getLast().getPropertyName();
			String allowed = Arrays.toString(ife.getTargetType().getEnumConstants());
			return ResponseEntity.badRequest().body(
				ErrorResponse.of(ErrorCode.INVALID_INPUT, field + ": 허용되지 않는 값입니다. 가능한 값: " + allowed));
		}
		return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		if (e instanceof org.springframework.web.ErrorResponse springError) {
			HttpStatusCode status = springError.getStatusCode();
			return ResponseEntity.status(status).body(new ErrorResponse(status.toString(), e.getMessage()));
		}

		log.error("Unhandled exception", e);
		ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		log.warn("DataIntegrityViolation: {}", e.getMostSpecificCause().getMessage());
		ErrorCode code = ErrorCode.DATA_CONFLICT;
		return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
	}
}
