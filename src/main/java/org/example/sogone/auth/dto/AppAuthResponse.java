package org.example.sogone.auth.dto;

public record AppAuthResponse(String accessToken, String refreshToken) implements AuthResponse {
}
