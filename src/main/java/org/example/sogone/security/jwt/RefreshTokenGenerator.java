package org.example.sogone.security.jwt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {
	private final SecureRandom secureRandom = new SecureRandom();

	public String generate() {
		byte[] bytes = new byte[64];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public String hashToken(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().encodeToString(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("해시 알고리즘을 찾을 수 없습니다.");
		}
	}
}
