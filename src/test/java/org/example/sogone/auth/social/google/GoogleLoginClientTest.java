package org.example.sogone.auth.social.google;

import static org.assertj.core.api.Assertions.*;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.SocialUserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class GoogleLoginClientTest {

	private final GoogleLoginClient client = new GoogleLoginClient(token -> Jwt.withTokenValue(token)
		.header("alg", "RS256")
		.subject("google-sub")
		.claim("email", "A@Gmail.com")
		.claim("email_verified", true)
		.claim("name", "구글이름")
		.build());

	@Test
	void ID_token을_SocialUserInfo로_변환한다() {
		SocialUserInfo info = client.getUserInfo(
			new SocialLoginRequestDTO("token", null, "요청닉네임", Platform.IOS));

		assertThat(info.provider()).isEqualTo(Provider.GOOGLE);
		assertThat(info.providerUserId()).isEqualTo("google-sub");
		assertThat(info.email()).isEqualTo("a@gmail.com");
		assertThat(info.emailVerified()).isTrue();
		assertThat(info.nickname()).isEqualTo("구글이름");   // 요청 닉네임이 아니라 토큰의 name 사용
	}
}
