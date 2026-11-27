package org.example.sogone.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;

import org.example.sogone.security.jwt.JwtProperties;
import org.example.sogone.security.jwt.JwtTokenProvider;
import org.example.sogone.support.IntegrationTestSupport;
import org.example.sogone.user.entity.User;
import org.example.sogone.user.entity.type.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

/**
 * 보호된 API가 아직 없어서 존재하지 않는 경로로 검증한다.
 * Security를 통과하면 404, 막히면 401/403.
 */
class AuthorizationIntegrationTest extends IntegrationTestSupport {

	private static final String PROTECTED_URL = "/api/test/protected";
	private static final String ADMIN_URL = "/api/admin/test";

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private JwtProperties jwtProperties;

	@Test
	void 토큰_없이_보호된_API를_호출하면_401() throws Exception {
		mockMvc.perform(get(PROTECTED_URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
	}

	@Test
	void 형식이_잘못된_토큰이면_401_INVALID_TOKEN() throws Exception {
		requestWithToken(PROTECTED_URL, "invalid.token.value")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("INVALID_TOKEN"));
	}

	@Test
	void 만료된_토큰이면_401_TOKEN_EXPIRED() throws Exception {
		JwtProperties expired = new JwtProperties();
		expired.setSecretKey(jwtProperties.getSecretKey());
		expired.setAccessExpiration(Duration.ofMinutes(-1));
		String token = new JwtTokenProvider(expired).generateAccessToken(user(1L, Role.USER));

		requestWithToken(PROTECTED_URL, token)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.data.code").value("TOKEN_EXPIRED"));
	}

	@Test
	void 유효한_토큰이면_인증을_통과한다() throws Exception {
		String token = data(signup(uniqueEmail(), "IOS")).path("accessToken").asString();

		requestWithToken(PROTECTED_URL, token).andExpect(status().isNotFound());
	}

	@Test
	void USER_권한으로_관리자_API를_호출하면_403() throws Exception {
		requestWithToken(ADMIN_URL, jwtTokenProvider.generateAccessToken(user(1L, Role.USER)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.data.code").value("FORBIDDEN"));
	}

	@Test
	void ADMIN_권한이면_관리자_API를_통과한다() throws Exception {
		requestWithToken(ADMIN_URL, jwtTokenProvider.generateAccessToken(user(1L, Role.ADMIN)))
			.andExpect(status().isNotFound());
	}

	private ResultActions requestWithToken(String url, String token) throws Exception {
		return mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
	}

	private User user(Long id, Role role) {
		User user = User.builder().nickname("tester").build();
		ReflectionTestUtils.setField(user, "id", id);
		ReflectionTestUtils.setField(user, "role", role);
		return user;
	}
}
