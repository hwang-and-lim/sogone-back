package org.example.sogone.auth.dto;

import java.time.Instant;

import org.example.sogone.auth.entity.type.Platform;

public record TokenPair(String accessToken, String refreshToken, Instant refreshExpiredAt, Platform platform) {
}
