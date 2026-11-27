package org.example.sogone.auth.service;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.validator.routines.EmailValidator;
import org.example.sogone.auth.dto.LoginRequestDTO;
import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.auth.entity.UserSession;
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
import org.example.sogone.util.EmailNormalizer;
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
	private final SessionService sessionService;
	private final EmailVerifyService emailVerifyService;

	@Transactional
	public TokenPair signup(SignupRequestDTO request) {
		String email = EmailNormalizer.normalizeEmail(request.getEmail());
		if (!EmailValidator.getInstance().isValid(email)) {
			throw new BusinessException(ErrorCode.INVALID_EMAIL);
		}

		emailVerifyService.consumeVerifiedEmail(email);

		if (authProviderRepository.existsByProviderAndProviderUserId(Provider.LOCAL, email)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		// 같은 이메일의 소셜 회원이 있으면 그 사용자에 연결, 없으면 신규 가입 (연결 시 요청 닉네임은 무시)
		User user = findLinkableSocialUser(email).orElseGet(() -> userService.save(request.getNickname()));

		UserAuthProvider newAuthProvider = UserAuthProvider.ofLocal(
			user,
			email,
			passwordEncoder.encode(request.getPassword()),
			Instant.now()
		);
		authProviderRepository.save(newAuthProvider);

		return sessionService.createSession(user, request.getPlatform());
	}

	// 제공자가 이메일을 인증한 소셜 계정만 연결 (인증 안 된 이메일로 만든 계정에 묶이는 것 방지)
	private Optional<User> findLinkableSocialUser(String email) {
		Optional<User> linkable = authProviderRepository.findAllByEmail(email).stream()
			.filter(p -> p.getProvider() != Provider.LOCAL)
			.filter(UserAuthProvider::isEmailVerified)
			.map(UserAuthProvider::getUser)
			.findFirst();
		linkable.ifPresent(User::validateActive);

		return linkable;
	}

	@Transactional(noRollbackFor = BusinessException.class)
	public TokenPair login(LoginRequestDTO request) {
		String email = EmailNormalizer.normalizeEmail(request.getEmail());
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

		return sessionService.createSession(userAuthProvider.getUser(), request.getPlatform());
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

		return new TokenPair(accessToken, newRefreshToken, refreshExpiresAt, session.getPlatform(),
			session.getUser().isNewUser());
	}

	@Transactional
	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank())
			return;

		String hash = refreshTokenGenerator.hashToken(refreshToken);
		userSessionRepository.findByRefreshTokenHash(hash).ifPresent(userSessionRepository::delete);
	}

}
