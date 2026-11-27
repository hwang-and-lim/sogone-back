package org.example.sogone.auth.social.kakao;

import static org.assertj.core.api.Assertions.*;

import org.example.sogone.auth.social.SocialUserInfo;
import org.example.sogone.auth.social.kakao.KakaoResponse.KakaoUserResponse;
import org.example.sogone.auth.social.kakao.KakaoResponse.KakaoUserResponse.KakaoAccount;
import org.example.sogone.auth.social.kakao.KakaoResponse.KakaoUserResponse.Profile;
import org.junit.jupiter.api.Test;

class KakaoResponseTest {

	@Test
	void 카카오_사용자_정보를_SocialUserInfo로_변환한다() {
		KakaoUserResponse response = new KakaoUserResponse(12345L,
			new KakaoAccount("A@Kakao.com", true, true, new Profile("카카오")));

		SocialUserInfo info = response.toSocialUserInfo();

		assertThat(info.providerUserId()).isEqualTo("12345");
		assertThat(info.email()).isEqualTo("a@kakao.com");
		assertThat(info.emailVerified()).isTrue();
		assertThat(info.nickname()).isEqualTo("카카오");
	}

	@Test
	void 동의하지_않은_항목은_null로_처리한다() {
		SocialUserInfo info = new KakaoUserResponse(12345L, null).toSocialUserInfo();

		assertThat(info.email()).isNull();
		assertThat(info.emailVerified()).isFalse();
		assertThat(info.nickname()).isNull();
	}
}
