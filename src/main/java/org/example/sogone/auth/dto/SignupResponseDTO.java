package org.example.sogone.auth.dto;

import java.time.Instant;

import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.user.entity.User;

public record SignupResponseDTO(Long userId, String nickname, Instant createdAt, String email, String accessToken,
                                String refreshToken) {
	public static SignupResponseDTO of(User user, UserAuthProvider provider, String accessToken, String refreshToken) {
		return new SignupResponseDTO(
			user.getId(),
			user.getNickname(),
			user.getCreatedAt(),
			provider.getEmail(),
			accessToken,
			refreshToken
		);
	}
}
