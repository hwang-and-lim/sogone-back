package org.example.sogone.auth.dto;

import org.example.sogone.auth.entity.type.Platform;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequestDTO(
	String token,
	String code,
	@NotNull(message = "플랫폼을 입력하세요.") Platform platform
) {
	@AssertTrue(message = "token 또는 code가 필요합니다.")
	public boolean isCredentialPresent() {
		return (token != null && !token.isBlank()
			|| (code != null && !code.isBlank()));
	}
}
