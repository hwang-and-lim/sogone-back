package org.example.sogone.auth.service;

import java.time.Instant;

import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.UserSession;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.auth.repository.UserSessionRepository;
import org.example.sogone.security.jwt.JwtProperties;
import org.example.sogone.security.jwt.JwtTokenProvider;
import org.example.sogone.security.jwt.RefreshTokenGenerator;
import org.example.sogone.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SessionService {
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenGenerator refreshTokenGenerator;
	private final JwtProperties jwtProperties;
	private final UserSessionRepository userSessionRepository;

	@Transactional
	public TokenPair createSession(User user, Platform platform) {
		String accessToken = jwtTokenProvider.generateAccessToken(user);
		String refreshToken = refreshTokenGenerator.generate();
		Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshExpiration());

		userSessionRepository.deleteUserSessionByUserAndPlatform(user, platform);

		userSessionRepository.save(UserSession.builder()
			.user(user)
			.platform(platform)
			.refreshTokenHash(refreshTokenGenerator.hashToken(refreshToken))
			.expiresAt(expiresAt)
			.build());

		return new TokenPair(accessToken, refreshToken, expiresAt, platform, user.isNewUser());
	}
}
