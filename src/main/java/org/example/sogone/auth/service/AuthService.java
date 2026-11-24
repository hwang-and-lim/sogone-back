package org.example.sogone.auth.service;

import java.time.Instant;

import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.SignupResponseDTO;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.auth.entity.UserSession;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.repository.UserAuthProviderRepository;
import org.example.sogone.auth.repository.UserSessionRepository;
import org.example.sogone.config.jwt.JwtTokenProvider;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.user.entity.User;
import org.example.sogone.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserService userService;
	private final UserAuthProviderRepository authProviderRepository;
	private final UserSessionRepository userSessionRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignupResponseDTO signup(SignupRequestDTO request) {
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

		TokenPair tokens = jwtTokenProvider.issueToken(newUser);

		UserSession newUserSession = UserSession.builder()
			.user(newUser)
			.platform(request.getPlatform())
			.refreshTokenHash(tokens.refreshToken())
			.expiresAt(tokens.refreshExpiredAt())
			.build();

		userSessionRepository.save(newUserSession);

		return SignupResponseDTO.of(newUser, newAuthProvider, tokens.accessToken(), tokens.refreshToken());
	}
}
