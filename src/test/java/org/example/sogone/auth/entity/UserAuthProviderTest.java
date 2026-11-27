package org.example.sogone.auth.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class UserAuthProviderTest {

	private final Instant now = Instant.parse("2026-01-01T00:00:00Z");

	private UserAuthProvider localProvider() {
		return UserAuthProvider.ofLocal(null, "a@test.com", "hash", now);
	}

	@Test
	void 로그인_5회_실패하면_1시간_잠긴다() {
		UserAuthProvider provider = localProvider();

		for (int i = 0; i < 5; i++) {
			provider.recordLoginFailure(now);
		}

		assertThat(provider.isLocked(now)).isTrue();
		assertThat(provider.isLocked(now.plus(Duration.ofHours(1)))).isFalse();
	}

	@Test
	void 로그인_성공하면_실패_횟수와_잠금이_초기화된다() {
		UserAuthProvider provider = localProvider();
		provider.recordLoginFailure(now);

		provider.recordLoginSuccess();

		assertThat(provider.getFailedLoginCount()).isZero();
		assertThat(provider.isLocked(now)).isFalse();
	}

	@Test
	void 이메일은_소문자로_정규화된다() {
		assertThat(UserAuthProvider.normalizeEmail("  A@Test.COM ")).isEqualTo("a@test.com");
	}
}
