package org.example.sogone.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.example.sogone.exception.ErrorCode;
import org.example.sogone.user.entity.User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	private final SecretKey key;
	private final Duration accessExpiration;
	private final static String TOKEN_TYPE = "type";
	private final static String ACCESS_TOKEN = "access-token";
	private final static String ROLE_CLAIM = "role";

	public JwtTokenProvider(JwtProperties jwtProperties) {
		if (jwtProperties.getSecretKey() == null || jwtProperties.getSecretKey().isBlank()) {
			throw new IllegalStateException("jwt.secret을 찾을 없습니다.");
		}

		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey()));
		this.accessExpiration = jwtProperties.getAccessExpiration();
	}

	public String generateAccessToken(User user) {
		Instant now = Instant.now();

		return Jwts.builder()
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plus(accessExpiration)))
			.subject(user.getId().toString())
			.claim(ROLE_CLAIM, user.getRole())
			.claim(TOKEN_TYPE, ACCESS_TOKEN)
			.signWith(key)
			.compact();
	}

	public Claims parseAccessToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		if (!ACCESS_TOKEN.equals(claims.get(TOKEN_TYPE, String.class))) {
			throw new JwtException(ErrorCode.INVALID_TOKEN.getMessage());
		}

		return claims;
	}
}
