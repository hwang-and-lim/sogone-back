package org.example.sogone.auth.social.apple;

import java.util.List;

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
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class AppleLoginClient implements SocialLoginClient {
	private static final String JWK_SET_URI = "https://appleid.apple.com/auth/keys";
	private static final String ISSUER = "https://appleid.apple.com";

	private final JwtDecoder decoder;

	@Autowired
	public AppleLoginClient(OAuthProperties properties) {
		this(createDecoder(properties.apple().clientIds()));
	}

	AppleLoginClient(JwtDecoder decoder) {
		this.decoder = decoder;
	}

	private static JwtDecoder createDecoder(List<String> clientIds) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();

		OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>(
			JwtClaimNames.AUD, aud -> aud != null && aud.stream().anyMatch((clientIds::contains))
		);

		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
			JwtValidators.createDefaultWithIssuer(ISSUER),
			audience
		));

		return decoder;
	}

	@Override
	public Provider provider() {
		return Provider.APPLE;
	}

	@Override
	public SocialUserInfo getUserInfo(SocialLoginRequestDTO request) {
		if (request.token() == null || request.token().isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
		}

		Jwt jwt = decode(request.token());

		return new SocialUserInfo(
			Provider.APPLE,
			jwt.getSubject(),
			jwt.getClaimAsString("email"),
			isTrue(jwt.getClaim("email_verified")),
			request.nickname()
		);
	}

	private boolean isTrue(Object value) {
		return Boolean.TRUE.equals(value) || "true".equals(value);
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
