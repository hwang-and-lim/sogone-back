package org.example.sogone.auth.dto;

public record TokenResponse(
	String accessToken,
	String refreshToken
) {
}
