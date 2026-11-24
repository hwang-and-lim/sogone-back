package org.example.sogone.user.entity;

import java.time.Instant;

import org.example.sogone.global.common.BaseTimeEntity;
import org.example.sogone.user.entity.type.Role;
import org.example.sogone.user.entity.type.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String nickname;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(length = 150)
	private String bio;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@Column(name = "onboarded_at")
	private Instant onboardedAt;

	@Column(name = "last_active_at")
	private Instant lastActiveAt;

	@Column(name = "restricted_until")
	private Instant restrictedUntil;

	@Column(name = "push_enabled", nullable = false)
	private Boolean pushEnabled;

	@Column(name = "push_like", nullable = false)
	private Boolean pushLike;

	@Column(name = "push_comment", nullable = false)
	private Boolean pushComment;

	@Column(name = "push_follow", nullable = false)
	private Boolean pushFollow;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Builder
	private User(String nickname) {
		this.nickname = nickname;
		this.role = Role.USER;
		this.status = UserStatus.ACTIVE;
		this.pushEnabled = true;
		this.pushLike = true;
		this.pushComment = true;
		this.pushFollow = true;
	}
}
