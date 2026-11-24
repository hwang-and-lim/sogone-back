package org.example.sogone.auth.controller;

import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.SignupResponseDTO;
import org.example.sogone.auth.service.AuthService;
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
	public ResponseEntity<SignupResponseDTO> signup(@RequestBody @Valid SignupRequestDTO request) {
		SignupResponseDTO responseDTO = authService.signup(request);

		return ResponseEntity.ok().body(responseDTO);
	}
}
