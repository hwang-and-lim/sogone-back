package org.example.sogone.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.OAuthProperties;
import org.example.sogone.auth.social.SocialUserInfo;
import org.example.sogone.auth.social.google.GoogleLoginClient;
import org.example.sogone.auth.social.kakao.KakaoLoginClient;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestClient;

/**
 * 카카오·구글 서버 대신 가짜 Client로 교체해서 계정 처리(로그인/연결/가입)만 검증한다.
 * 가짜 Client는 요청의 token을 키로 FAKE_USERS에서 사용자 정보를 꺼낸다.
 */
class SocialAuthIntegrationTest extends IntegrationTestSupport {

	private static final Map<String, SocialUserInfo> FAKE_USERS = new ConcurrentHashMap<>();
	private static final OAuthProperties FAKE_PROPERTIES = new OAuthProperties(
		new OAuthProperties.Kakao(1L, "", "", ""),
		new OAuthProperties.Google(List.of("fake")),
		new OAuthProperties.Apple(List.of("fake")));

	@TestBean
	KakaoLoginClient kakaoLoginClient;

	@TestBean
	GoogleLoginClient googleLoginClient;

	static KakaoLoginClient kakaoLoginClient() {
		return new KakaoLoginClient(RestClient.builder(), FAKE_PROPERTIES) {
			@Override
			public SocialUserInfo getUserInfo(SocialLoginRequestDTO request) {
				return fakeUser(request);
			}
		};
	}

	static GoogleLoginClient googleLoginClient() {
		return new GoogleLoginClient(FAKE_PROPERTIES) {
			@Override
			public SocialUserInfo getUserInfo(SocialLoginRequestDTO request) {
				return fakeUser(request);
			}
		};
	}

	private static SocialUserInfo fakeUser(SocialLoginRequestDTO request) {
		SocialUserInfo info = FAKE_USERS.get(request.token());
		if (info == null) {
			throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
		}
		return info;
	}

	@Test
	void 처음_로그인하면_가입하고_다시_로그인하면_같은_사용자다() throws Exception {
		String token = register(Provider.KAKAO, "k-1", null, false, uniqueNickname());

		socialLogin("kakao", token).andExpect(status().isOk());
		socialLogin("kakao", token).andExpect(status().isOk());

		assertThat(userCount(Provider.KAKAO, "k-1")).isEqualTo(1);
	}

	@Test
	void 인증된_같은_이메일의_소셜_계정이_있으면_연결한다() throws Exception {
		String email = uniqueEmail();
		socialLogin("kakao", register(Provider.KAKAO, "k-2", email, true, uniqueNickname()));
		socialLogin("google", register(Provider.GOOGLE, "g-2", email, true, uniqueNickname()));

		assertThat(userIdOf(Provider.GOOGLE, "g-2")).isEqualTo(userIdOf(Provider.KAKAO, "k-2"));
	}

	@Test
	void 같은_이메일이라도_이메일_계정과는_연결하지_않는다() throws Exception {
		String email = uniqueEmail();
		signup(email, "IOS");

		socialLogin("kakao", register(Provider.KAKAO, "k-3", email, true, uniqueNickname()))
			.andExpect(status().isOk());

		assertThat(userIdOf(Provider.KAKAO, "k-3")).isNotEqualTo(userIdOf(Provider.LOCAL, email));
	}

	@Test
	void 닉네임이_중복이면_임시_닉네임으로_가입한다() throws Exception {
		String nickname = uniqueNickname();
		socialLogin("kakao", register(Provider.KAKAO, "k-4", null, false, nickname));

		socialLogin("kakao", register(Provider.KAKAO, "k-5", null, false, nickname));

		String saved = jdbcTemplate.queryForObject("""
			select u.nickname from users u join user_auth_providers p on p.user_id = u.id
			where p.provider = 'KAKAO' and p.provider_user_id = 'k-5'
			""", String.class);
		assertThat(saved).startsWith("사용자_");
	}

	@Test
	void 이용_제한된_사용자는_로그인할_수_없다() throws Exception {
		String token = register(Provider.KAKAO, "k-6", null, false, uniqueNickname());
		socialLogin("kakao", token);
		jdbcTemplate.update("update users set status = 'RESTRICTED' where id = ?", userIdOf(Provider.KAKAO, "k-6"));

		socialLogin("kakao", token)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.data.code").value("USER_RESTRICTED"));
	}

	@Test
	void 소셜_토큰_검증에_실패하면_401() throws Exception {
		socialLogin("kakao", "unknown-token")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("INVALID_SOCIAL_TOKEN"));
	}

	@Test
	void 지원하지_않는_provider는_400() throws Exception {
		socialLogin("naver", "any")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.data.code").value("UNSUPPORTED_PROVIDER"));
	}

	private String register(Provider provider, String providerUserId, String email, boolean verified, String nickname) {
		String token = provider + "-" + providerUserId;
		FAKE_USERS.put(token, new SocialUserInfo(provider, providerUserId, email, verified, nickname));
		return token;
	}

	private ResultActions socialLogin(String provider, String token) throws Exception {
		return postJson("/api/auth/social/" + provider, """
			{"token":"%s","platform":"IOS"}
			""".formatted(token));
	}

	private Long userIdOf(Provider provider, String providerUserId) {
		return jdbcTemplate.queryForObject(
			"select user_id from user_auth_providers where provider = ? and provider_user_id = ?",
			Long.class, provider.name(), providerUserId);
	}

	private Long userCount(Provider provider, String providerUserId) {
		return jdbcTemplate.queryForObject(
			"select count(distinct user_id) from user_auth_providers where provider = ? and provider_user_id = ?",
			Long.class, provider.name(), providerUserId);
	}
}
