package org.example.sogone.auth.social;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth")
public record OAuthProperties(Kakao kakao) {
	public record Kakao(long appId, String restApiKey, String clientSecret, String redirectUri) {
	}
}
