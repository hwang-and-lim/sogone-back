package org.example.sogone.auth.service;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.dto.TokenPair;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.SocialLoginClientRegistry;
import org.example.sogone.auth.social.SocialUserInfo;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialAuthService {
	private final SocialLoginClientRegistry clientRegistry;
	private final SocialAccountService socialAccountService;

	public TokenPair login(Provider provider, SocialLoginRequestDTO request) {
		SocialUserInfo info = clientRegistry.get(provider).getUserInfo(request);
		return socialAccountService.loginOrSignup(info, request.platform());
	}
}
