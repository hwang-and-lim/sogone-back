package org.example.sogone.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 통합 테스트 공통 설정.
 * - DB: Testcontainers PostgreSQL (Docker 필요)
 * - 외부 값(.env)에 의존하지 않도록 JWT / OAuth 설정을 테스트용 값으로 고정
 */
@SpringBootTest(properties = {
	"jwt.secret-key=c29nb25lLXRlc3Qtb25seS1qd3Qtc2VjcmV0LWtleS0wMTIzNDU2Nzg5",
	"oauth.kakao.app-id=1",
	"oauth.kakao.rest-api-key=test",
	"oauth.kakao.client-secret=test",
	"oauth.kakao.redirect-uri=http://localhost:3000/callback",
	"oauth.google.client-ids=test-google-client",
	"oauth.apple.client-ids=test-apple-client"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestSupport {

	protected static final String PASSWORD = "password123";

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	protected StringRedisTemplate redisTemplate;

	/** 테스트끼리 데이터가 겹치지 않도록 고유한 이메일 생성 */
	protected String uniqueEmail() {
		return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
	}

	/** 2~30자 닉네임 규칙에 맞는 고유 닉네임 생성 */
	protected String uniqueNickname() {
		return "nick-" + UUID.randomUUID().toString().substring(0, 8);
	}

	protected ResultActions postJson(String url, String body) throws Exception {
		return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	/** 이메일 인증을 마친 상태로 회원가입 */
	protected ResultActions signup(String email, String platform) throws Exception {
		markEmailVerified(email);
		return postJson("/api/auth/signup", """
			{"email":"%s","password":"%s","nickname":"%s","platform":"%s"}
			""".formatted(email, PASSWORD, uniqueNickname(), platform));
	}

	/** 인증 코드 확인 단계를 건너뛰고 인증 완료 표시만 저장 */
	protected void markEmailVerified(String email) {
		redisTemplate.opsForValue().set("email-verify:verified:" + email, "1", Duration.ofMinutes(5));
	}

	protected ResultActions login(String email, String password, String platform) throws Exception {
		return postJson("/api/auth/login", """
			{"email":"%s","password":"%s","platform":"%s"}
			""".formatted(email, password, platform));
	}

	/** 응답 body의 data 필드 */
	protected JsonNode data(ResultActions result) throws Exception {
		String body = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		return objectMapper.readTree(body).path("data");
	}
}
