package org.example.sogone.security.jwt;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieManager {
	private final JwtProperties.Cookie cookieProps;
	private final Duration maxAge;

	public RefreshTokenCookieManager(JwtProperties properties) {
		this.cookieProps = properties.getCookie();
		this.maxAge = properties.getRefreshExpiration();
	}

	public String cookieName() {
		return cookieProps.name();
	}

	public ResponseCookie create(String refreshToken) {
		return build(refreshToken, maxAge);
	}

	public ResponseCookie delete() {
		return build("", Duration.ZERO);
	}

	private ResponseCookie build(String value, Duration age) {
		return ResponseCookie.from(cookieProps.name(), value)
			.httpOnly(true)
			.secure(cookieProps.secure())
			.sameSite(cookieProps.sameSite())
			.path(cookieProps.path())
			.maxAge(age)
			.build();
	}
}
