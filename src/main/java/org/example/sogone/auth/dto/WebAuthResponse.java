package org.example.sogone.auth.dto;

public record WebAuthResponse(String accessToken, boolean isNewUser) implements AuthResponse {
}
