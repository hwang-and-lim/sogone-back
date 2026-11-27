package org.example.sogone.auth.service;

import java.util.Optional;

import org.example.sogone.auth.component.NicknameGenerator;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.repository.UserAuthProviderRepository;
import org.example.sogone.auth.social.SocialUserInfo;
import org.example.sogone.user.entity.User;
import org.example.sogone.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialAccountService {
	private final UserAuthProviderRepository authProviderRepository;
	private final UserService userService;
	private final NicknameGenerator nicknameGenerator;
	private final SessionService sessionService;

	@Transactional
	public TokenPair loginOrSignup(SocialUserInfo info, Platform platform) {
		// 이미 연결된 소셜 계정
		Optional<UserAuthProvider> linked = authProviderRepository.findByProviderAndProviderUserId(info.provider(),
			info.providerUserId());
		if (linked.isPresent()) {
			linked.get().getUser().validateActive();

			return sessionService.createSession(linked.get().getUser(), platform);
		}

		// 연결 가능한 기존 계정 -> 없으면 신규 가입
		User user = findLinkableUser(info).orElseGet(() ->
			userService.save(nicknameGenerator.resolve(info.nickname())));

		authProviderRepository.save(UserAuthProvider.ofSocial(
			user, info.provider(), info.providerUserId(), info.email()));

		return sessionService.createSession(user, platform);
	}

	// 자동 연결 : 양쪽 이메일이 모두 인증된 경우만
	private Optional<User> findLinkableUser(SocialUserInfo info) {
		if (!info.emailVerified() || info.email() == null) {
			return Optional.empty();
		}

		return authProviderRepository.findAllByEmail(info.email()).stream()
			.filter(p -> p.getProvider() != Provider.LOCAL)
			.map(UserAuthProvider::getUser)
			.findFirst();
	}
}
