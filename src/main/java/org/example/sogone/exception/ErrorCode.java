package org.example.sogone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// 400 Bad Request
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),

	// 401 Unauthorized
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access Token 이 만료되었습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token 입니다."),

	// 403 Forbidden
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	USER_RESTRICTED(HttpStatus.FORBIDDEN, "제한된 계정입니다."),

	// 404 Not Found
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "곡을 찾을 수 없습니다."),
	ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."),
	ARTIST_NOT_FOUND(HttpStatus.NOT_FOUND, "아티스트를 찾을 수 없습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "컬렉션을 찾을 수 없습니다."),
	GENRE_NOT_FOUND(HttpStatus.NOT_FOUND, "장르를 찾을 수 없습니다."),
	TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."),

	// 405 Not Allowed
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

	// 409 Conflict
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	DATA_CONFLICT(HttpStatus.CONFLICT, "요청이 충돌되었습니다."),

	// 415
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),

	// 423 Locked
	ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "로그인 시도가 많아 계정이 잠겼습니다. 잠시 후 다시 시도해주세요."),

	// 429 Too Many Requests
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

	// 500
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

	// 503 Service Unavailable
	EXTERNAL_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "외부 음악 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public static ErrorCode toErrorCode(HttpStatusCode statusCode) {
		return switch (statusCode.value()) {
			case 400 -> ErrorCode.INVALID_INPUT;
			case 401 -> ErrorCode.UNAUTHORIZED;
			case 403 -> ErrorCode.FORBIDDEN;
			case 404 -> ErrorCode.NOT_FOUND;
			case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
			case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
			case 429 -> ErrorCode.TOO_MANY_REQUESTS;
			default -> ErrorCode.INTERNAL_SERVER_ERROR;
		};
	}
}
