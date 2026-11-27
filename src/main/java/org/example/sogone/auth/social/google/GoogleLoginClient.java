package org.example.sogone.auth.social.google;

import java.util.List;
import java.util.Set;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.OAuthProperties;
import org.example.sogone.auth.social.SocialLoginClient;
import org.example.sogone.auth.social.SocialUserInfo;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class GoogleLoginClient implements SocialLoginClient {
	private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
	private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

	private final JwtDecoder decoder;

	@Autowired
	public GoogleLoginClient(OAuthProperties properties) {
		this(createDecoder(properties.google().clientIds()));
	}

	GoogleLoginClient(JwtDecoder decoder) {     // 테스트에서 가짜 decoder를 넣기 위한 생성자
		this.decoder = decoder;
	}

	private static JwtDecoder createDecoder(List<String> clientIds) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();

		OAuth2TokenValidator<Jwt> issuer = new JwtClaimValidator<String>(
			JwtClaimNames.ISS, ISSUERS::contains
		);
		OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>(
			JwtClaimNames.AUD, aud -> aud != null && aud.stream().anyMatch(clientIds::contains)
		);

		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
			new JwtTimestampValidator(),
			issuer,
			audience
		));

		return decoder;
	}

	@Override
	public Provider provider() {
		return Provider.GOOGLE;
	}

	@Override
	public SocialUserInfo getUserInfo(SocialLoginRequestDTO request) {
		if (request.token() == null || request.token().isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
		}

		Jwt jwt = decode(request.token());

		return new SocialUserInfo(
			Provider.GOOGLE,
			jwt.getSubject(),
			jwt.getClaimAsString("email"),
			Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")),
			jwt.getClaimAsString("name")
		);
	}

	private Jwt decode(String idToken) {
		try {
			return decoder.decode(idToken);
		} catch (BadJwtException e) {
			throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
		} catch (JwtException e) {
			throw new BusinessException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
		}
	}
}