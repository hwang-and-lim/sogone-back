package org.example.sogone.auth.controller;

import org.example.sogone.auth.dto.AuthResponseDTO;
import org.example.sogone.auth.dto.LoginRequestDTO;
import org.example.sogone.auth.dto.ReissueRequestDTO;
import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.TokenResponse;
import org.example.sogone.auth.service.AuthService;
import org.example.sogone.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<AuthResponseDTO>> signup(@RequestBody @Valid SignupRequestDTO request) {
		AuthResponseDTO response = authService.signup(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody @Valid LoginRequestDTO request) {
		AuthResponseDTO response = authService.login(request);

		return ResponseEntity.ok().body(ApiResponse.ok(response));
	}

	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<TokenResponse>> reissue(@RequestBody @Valid ReissueRequestDTO request) {
		TokenResponse response = authService.reissue(request);

		return ResponseEntity.ok().body(ApiResponse.ok(response));
	}
}
