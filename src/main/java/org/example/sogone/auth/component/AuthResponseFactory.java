package org.example.sogone.auth.component;

import org.example.sogone.auth.dto.AppAuthResponse;
import org.example.sogone.auth.dto.AuthResponse;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.dto.WebAuthResponse;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.global.common.ApiResponse;
import org.example.sogone.security.jwt.RefreshTokenCookieManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthResponseFactory {
	private final RefreshTokenCookieManager cookieManager;

	public ResponseEntity<ApiResponse<AuthResponse>> tokenResponse(HttpStatus status, TokenPair tokens) {
		if (tokens.platform() == Platform.WEB) {
			return ResponseEntity.status(status)
				.header(HttpHeaders.SET_COOKIE, cookieManager.create(tokens.refreshToken()).toString())
				.body(ApiResponse.ok(new WebAuthResponse(tokens.accessToken(), tokens.isNewUser())));
		}

		return ResponseEntity.status(status)
			.body(ApiResponse.ok(new AppAuthResponse(tokens.accessToken(), tokens.refreshToken(), tokens.isNewUser())));
	}
}
