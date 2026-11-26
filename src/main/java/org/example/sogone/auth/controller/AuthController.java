package org.example.sogone.auth.controller;

import org.example.sogone.auth.dto.AppAuthResponse;
import org.example.sogone.auth.dto.AuthResponse;
import org.example.sogone.auth.dto.LoginRequestDTO;
import org.example.sogone.auth.dto.RefreshTokenRequestDTO;
import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.dto.WebAuthResponse;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.auth.service.AuthService;
import org.example.sogone.global.common.ApiResponse;
import org.example.sogone.security.jwt.RefreshTokenCookieManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final RefreshTokenCookieManager cookieManager;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<AuthResponse>> signup(@RequestBody @Valid SignupRequestDTO request) {
		TokenPair tokens = authService.signup(request);

		return tokenResponse(HttpStatus.CREATED, tokens);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequestDTO request) {
		TokenPair tokens = authService.login(request);

		return tokenResponse(HttpStatus.OK, tokens);
	}

	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<AuthResponse>> reissue(
		@CookieValue(name = "${jwt.cookie.name}", required = false) String cookieRefreshToken,
		@RequestBody(required = false) RefreshTokenRequestDTO request
	) {
		String refreshToken =
			(cookieRefreshToken != null) ? cookieRefreshToken :
				(request != null) ? request.refreshToken() : null;

		TokenPair tokens = authService.reissue(refreshToken);

		return tokenResponse(HttpStatus.OK, tokens);
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
		@CookieValue(name = "${jwt.cookie.name}", required = false) String cookieRefreshToken,
		@RequestBody(required = false) RefreshTokenRequestDTO request
	) {
		String refreshToken =
			(cookieRefreshToken != null) ? cookieRefreshToken :
				(request != null) ? request.refreshToken() : null;

		authService.logout(refreshToken);

		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		if (cookieRefreshToken != null) {
			builder.header(HttpHeaders.SET_COOKIE, cookieManager.delete().toString());
		}

		return builder.body(ApiResponse.ok(null));
	}

	private ResponseEntity<ApiResponse<AuthResponse>> tokenResponse(HttpStatus status, TokenPair tokens) {
		if (tokens.platform() == Platform.WEB) {
			return ResponseEntity.status(status)
				.header(HttpHeaders.SET_COOKIE, cookieManager.create(tokens.refreshToken()).toString())
				.body(ApiResponse.ok(new WebAuthResponse(tokens.accessToken())));
		}

		return ResponseEntity.status(status)
			.body(ApiResponse.ok(new AppAuthResponse(tokens.accessToken(), tokens.refreshToken())));
	}
}
