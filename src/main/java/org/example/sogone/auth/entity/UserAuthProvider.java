package org.example.sogone.auth.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.global.common.BaseCreatedTimeEntity;
import org.example.sogone.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "user_auth_providers",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uq_user_auth_providers_provider_user",
			columnNames = {"provider", "provider_user_id"}
		)
	}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthProvider extends BaseCreatedTimeEntity {
	private final static int MAX_LOGIN_ATTEMPT = 5;
	private final static Duration DURATION_LOCKED = Duration.ofHours(1);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	@Column(nullable = false, name = "provider_user_id")
	private String providerUserId;

	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(name = "password_changed_at")
	private Instant passwordChangedAt;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;

	@Column(nullable = false, name = "failed_login_count")
	private int failedLoginCount;

	@Column(name = "locked_until")
	private Instant lockedUntil;

	@Builder
	private UserAuthProvider(
		User user,
		Provider provider,
		String providerUserId,
		String email,
		String passwordHash,
		Instant now
	) {
		this.user = user;
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.failedLoginCount = 0;
		this.passwordChangedAt = passwordHash != null ? now : null;
	}

	// 로컬 회원 가입
	public static UserAuthProvider ofLocal(User user, String email, String passwordHash, Instant now) {
		String normalized = normalizeEmail(email);
		return new UserAuthProvider(user, Provider.LOCAL, normalized, normalized, passwordHash, now);
	}

	// 소셜로그인
	public static UserAuthProvider ofSocial(User user, Provider provider, String providerUserId, String email) {
		return new UserAuthProvider(user, provider, providerUserId, email, null, null);
	}

	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	public void recordLoginFailure(Instant now) {
		this.failedLoginCount++;
		if (failedLoginCount >= MAX_LOGIN_ATTEMPT) {
			this.failedLoginCount = 0;
			this.lockedUntil = now.plus(DURATION_LOCKED);
		}
	}

	public void recordLoginSuccess() {
		this.failedLoginCount = 0;
		this.lockedUntil = null;
	}

	public boolean isLocked(Instant now) {
		if (lockedUntil == null)
			return false;

		return now.isBefore(lockedUntil);
	}
}
