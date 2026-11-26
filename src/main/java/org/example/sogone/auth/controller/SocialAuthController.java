package org.example.sogone.auth.controller;

import java.util.Locale;

import org.example.sogone.auth.component.AuthResponseFactory;
import org.example.sogone.auth.dto.AuthResponse;
import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.service.SocialAuthService;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {
	private final SocialAuthService socialAuthService;
	private final AuthResponseFactory authResponseFactory;

	@PostMapping("/{provider}")
	public ResponseEntity<ApiResponse<AuthResponse>> login(
		@PathVariable String provider,
		@RequestBody @Valid SocialLoginRequestDTO request
	) {
		TokenPair tokens = socialAuthService.login(toProvider(provider), request);

		return authResponseFactory.tokenResponse(HttpStatus.OK, tokens);
	}

	private Provider toProvider(String value) {
		try {
			return Provider.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
		}
	}
}
