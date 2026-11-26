package org.example.sogone.auth.dto;

public sealed interface AuthResponse permits WebAuthResponse, AppAuthResponse {
}
