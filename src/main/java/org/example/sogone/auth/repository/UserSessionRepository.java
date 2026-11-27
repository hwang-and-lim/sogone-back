package org.example.sogone.auth.repository;

import java.util.Optional;

import org.example.sogone.auth.entity.UserSession;
import org.example.sogone.auth.entity.type.Platform;
import org.example.sogone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
	Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
	
	void deleteUserSessionByUserAndPlatform(User user, Platform platform);
}
