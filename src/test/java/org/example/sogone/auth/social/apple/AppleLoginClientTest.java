package org.example.sogone.auth.social.apple;

import static org.assertj.core.api.Assertions.*;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.SocialUserInfo;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class AppleLoginClientTest {

	// 서명 검증은 라이브러리(NimbusJwtDecoder)에 맡기고, claim → SocialUserInfo 변환만 검증
	private final JwtDecoder fakeDecoder = token -> {
		if (token.equals("invalid")) {
			throw new BadJwtException("invalid");
		}
		return Jwt.withTokenValue(token)
			.header("alg", "ES256")
			.subject("001234.apple-sub")
			.claim("email", "A@Icloud.com")
			.claim("email_verified", "true")   // 애플은 문자열로 줄 수 있음
			.build();
	};

	private final AppleLoginClient client = new AppleLoginClient(fakeDecoder);

	@Test
	void identity_token을_SocialUserInfo로_변환한다() {
		SocialUserInfo info = client.getUserInfo(new SocialLoginRequestDTO("token", null, "동영", Platform.IOS));

		assertThat(info.provider()).isEqualTo(Provider.APPLE);
		assertThat(info.providerUserId()).isEqualTo("001234.apple-sub");
		assertThat(info.email()).isEqualTo("a@icloud.com");
		assertThat(info.emailVerified()).isTrue();
		assertThat(info.nickname()).isEqualTo("동영");   // 토큰에 이름이 없어 요청 값 사용
	}

	@Test
	void 검증에_실패한_토큰은_INVALID_SOCIAL_TOKEN() {
		assertThatThrownBy(() -> client.getUserInfo(new SocialLoginRequestDTO("invalid", null, null, Platform.IOS)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.INVALID_SOCIAL_TOKEN);
	}
}
