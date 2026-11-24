package org.example.sogone.auth.entity;

import java.time.Instant;

import org.example.sogone.auth.entity.type.Platform;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_sessions")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSession extends BaseCreatedTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true, length = 64)
	private String refreshTokenHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Platform platform;

	@Column(unique = true, name = "push_token")
	private String pushToken;

	@Column(nullable = false)
	private Instant expiresAt;

	@Builder
	private UserSession(User user, String refreshTokenHash, Platform platform, Instant expiresAt) {
		this.user = user;
		this.refreshTokenHash = refreshTokenHash;
		this.platform = platform;
		this.expiresAt = expiresAt;
	}
}
