package org.example.sogone.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.example.sogone.auth.dto.TokenPair;
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
	private final Duration refreshExpiration;
	private final static String TOKEN_TYPE = "type";
	private final static String ACCESS_TOKEN = "access-token";
	private final static String REFRESH_TOKEN = "refresh-token";

	public JwtTokenProvider(JwtProperties jwtProperties) {
		if (jwtProperties.getSecretKey() == null || jwtProperties.getSecretKey().isBlank()) {
			throw new IllegalStateException("jwt.secret이 없습니다.");
		}

		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey()));
		this.accessExpiration = jwtProperties.getAccessExpiration();
		this.refreshExpiration = jwtProperties.getRefreshExpiration();
	}

	public TokenPair issueToken(User user) {
		Instant now = Instant.now();
		Instant accessExpiresAt = now.plus(this.accessExpiration);
		Instant refreshExpiresAt = now.plus(this.refreshExpiration);

		String accessToken = generateAccessToken(user, accessExpiresAt, now);
		String refreshToken = generateRefreshToken(user, refreshExpiresAt, now);

		return new TokenPair(accessToken, refreshToken, refreshExpiresAt);
	}

	private String generateAccessToken(User user, Instant expiresAt, Instant now) {
		return generateToken(user, ACCESS_TOKEN, expiresAt, now);
	}

	private String generateRefreshToken(User user, Instant expiresAt, Instant now) {
		return generateToken(user, REFRESH_TOKEN, expiresAt, now);
	}

	private String generateToken(User user, String type, Instant expiresAt, Instant now) {
		return Jwts.builder()
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.subject(user.getId().toString())
			.claim("role", user.getRole())
			.claim(TOKEN_TYPE, type)
			.signWith(key)
			.compact();
	}

	public Claims parseAccessToken(String token) {
		return parse(token, ACCESS_TOKEN);
	}

	public Claims parseRefreshToken(String token) {
		return parse(token, REFRESH_TOKEN);
	}

	private Claims parse(String token, String type) {
		Claims claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		String actualType = claims.get(TOKEN_TYPE, String.class);
		if (!type.equals(actualType)) {
			throw new JwtException("토큰 타입이 올바르지 않습니다.");
		}

		return claims;
	}
}
