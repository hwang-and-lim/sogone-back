package org.example.sogone.auth.social;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth")
public record OAuthProperties(Kakao kakao, Google google, Apple apple) {
	public record Kakao(long appId, String restApiKey, String clientSecret, String redirectUri) {
	}

	public record Google(List<String> clientIds) {
	}

	public record Apple(List<String> clientIds) {
	}
}
