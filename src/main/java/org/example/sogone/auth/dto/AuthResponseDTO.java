package org.example.sogone.auth.dto;

import org.example.sogone.user.entity.User;

public record AuthResponseDTO(Long userId, String nickname, String accessToken, String refreshToken) {
	public static AuthResponseDTO of(User user, String accessToken, String refreshToken) {
		return new AuthResponseDTO(
			user.getId(),
			user.getNickname(),
			accessToken,
			refreshToken
		);
	}
}
