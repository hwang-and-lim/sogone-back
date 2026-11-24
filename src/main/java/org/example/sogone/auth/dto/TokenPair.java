package org.example.sogone.auth.dto;

import java.time.Instant;

public record TokenPair(String accessToken, String refreshToken, Instant refreshExpiredAt) {
}
