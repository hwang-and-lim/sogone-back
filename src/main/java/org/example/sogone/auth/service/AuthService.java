package org.example.sogone.auth.service;

import java.time.Instant;

import org.example.sogone.auth.dto.LoginRequestDTO;
import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.auth.entity.UserSession;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.repository.UserAuthProviderRepository;
import org.example.sogone.auth.repository.UserSessionRepository;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.security.jwt.JwtProperties;
import org.example.sogone.security.jwt.JwtTokenProvider;
import org.example.sogone.security.jwt.RefreshTokenGenerator;
import org.example.sogone.user.entity.User;
import org.example.sogone.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserService userService;
	private final UserAuthProviderRepository authProviderRepository;
	private final UserSessionRepository userSessionRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenGenerator refreshTokenGenerator;
	private final JwtProperties jwtProperties;

	@Transactional
	public TokenPair signup(SignupRequestDTO request) {
		String email = UserAuthProvider.normalizeEmail(request.getEmail());
		if (authProviderRepository.existsByProviderAndProviderUserId(Provider.LOCAL, email)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		User newUser = userService.save(request.getNickname());

		UserAuthProvider newAuthProvider = UserAuthProvider.ofLocal(
			newUser,
			email,
			passwordEncoder.encode(request.getPassword()),
			Instant.now()
		);
		authProviderRepository.save(newAuthProvider);

		return createSession(newUser, request.getPlatform());
	}

	@Transactional(noRollbackFor = BusinessException.class)
	public TokenPair login(LoginRequestDTO request) {
		String email = UserAuthProvider.normalizeEmail(request.getEmail());
		UserAuthProvider userAuthProvider = authProviderRepository.findByProviderAndProviderUserId(
				Provider.LOCAL,
				email)
			.orElseThrow(
				() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS)
			);

		Instant now = Instant.now();
		if (userAuthProvider.isLocked(now)) {
			throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
		}

		if (!passwordEncoder.matches(request.getPassword(), userAuthProvider.getPasswordHash())) {
			userAuthProvider.recordLoginFailure(now);

			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		userAuthProvider.recordLoginSuccess();

		return createSession(userAuthProvider.getUser(), request.getPlatform());
	}

	@Transactional
	public TokenPair reissue(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		String hash = refreshTokenGenerator.hashToken(refreshToken);
		UserSession session = userSessionRepository.findByRefreshTokenHash(hash).orElseThrow(
			() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
		);

		Instant now = Instant.now();
		if (session.isExpired(now)) {
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		String newRefreshToken = refreshTokenGenerator.generate();
		Instant refreshExpiresAt = now.plus(jwtProperties.getRefreshExpiration());

		session.rotate(
			refreshTokenGenerator.hashToken(newRefreshToken),
			refreshExpiresAt);

		String accessToken = jwtTokenProvider.generateAccessToken(session.getUser());

		return new TokenPair(accessToken, newRefreshToken, refreshExpiresAt, session.getPlatform());
	}

	@Transactional
	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank())
			return;

		String hash = refreshTokenGenerator.hashToken(refreshToken);
		userSessionRepository.findByRefreshTokenHash(hash).ifPresent(userSessionRepository::delete);
	}

	private TokenPair createSession(User user, Platform platform) {
		String accessToken = jwtTokenProvider.generateAccessToken(user);
		String refreshToken = refreshTokenGenerator.generate();
		Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshExpiration());

		userSessionRepository.save(UserSession.builder()
			.user(user)
			.platform(platform)
			.refreshTokenHash(refreshTokenGenerator.hashToken(refreshToken))
			.expiresAt(expiresAt)
			.build());

		return new TokenPair(accessToken, refreshToken, expiresAt, platform);
	}
}
