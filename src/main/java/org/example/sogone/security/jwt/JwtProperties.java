package org.example.sogone.security.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties("jwt")
public class JwtProperties {
	private String secretKey;
	private Duration accessExpiration;
	private Duration refreshExpiration;
	private Cookie cookie;

	public record Cookie(String name, String path, boolean secure, String sameSite) {
	}
}
