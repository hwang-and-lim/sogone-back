package org.example.sogone.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.sogone.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class LocalAuthIntegrationTest extends IntegrationTestSupport {

	@Test
	void 회원가입하면_토큰이_발급된다() throws Exception {
		signup(uniqueEmail(), "IOS")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
	}

	@Test
	void 이미_가입한_이메일이면_409() throws Exception {
		String email = uniqueEmail();
		signup(email, "IOS");

		signup(email, "IOS")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.data.code").value("DUPLICATE_EMAIL"));
	}

	@Test
	void 비밀번호가_틀리면_401() throws Exception {
		String email = uniqueEmail();
		signup(email, "IOS");

		login(email, "wrong-password", "IOS")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void 로그인_5회_실패하면_올바른_비밀번호도_잠금() throws Exception {
		String email = uniqueEmail();
		signup(email, "IOS");
		for (int i = 0; i < 5; i++) {
			login(email, "wrong-password", "IOS");
		}

		login(email, PASSWORD, "IOS")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("ACCOUNT_LOCKED"));
	}

	@Test
	void 웹은_Refresh_Token을_HttpOnly_쿠키로만_준다() throws Exception {
		String email = uniqueEmail();
		signup(email, "IOS");

		login(email, PASSWORD, "WEB")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.refreshToken").doesNotExist())
			.andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")));
	}

	@Test
	void 재발급하면_이전_Refresh_Token은_사용할_수_없다() throws Exception {
		String oldToken = data(signup(uniqueEmail(), "IOS")).path("refreshToken").asString();

		reissue(oldToken).andExpect(status().isOk());

		reissue(oldToken)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("INVALID_REFRESH_TOKEN"));
	}

	@Test
	void 로그아웃한_Refresh_Token으로는_재발급할_수_없다() throws Exception {
		String token = data(signup(uniqueEmail(), "IOS")).path("refreshToken").asString();

		postJson("/api/auth/logout", refreshTokenBody(token)).andExpect(status().isOk());

		reissue(token).andExpect(status().isUnauthorized());
	}

	@Test
	void 같은_플랫폼으로_다시_로그인하면_이전_세션은_삭제된다() throws Exception {
		String email = uniqueEmail();
		String first = data(signup(email, "IOS")).path("refreshToken").asString();

		login(email, PASSWORD, "IOS").andExpect(status().isOk());

		reissue(first).andExpect(status().isUnauthorized());
		Long sessions = jdbcTemplate.queryForObject("""
			select count(*) from user_sessions s
			join user_auth_providers p on p.user_id = s.user_id
			where p.provider_user_id = ? and s.platform = 'IOS'
			""", Long.class, email);
		assertThat(sessions).isEqualTo(1);
	}

	private org.springframework.test.web.servlet.ResultActions reissue(String refreshToken) throws Exception {
		return postJson("/api/auth/reissue", refreshTokenBody(refreshToken));
	}

	private String refreshTokenBody(String refreshToken) {
		return """
			{"refreshToken":"%s"}
			""".formatted(refreshToken);
	}
}
