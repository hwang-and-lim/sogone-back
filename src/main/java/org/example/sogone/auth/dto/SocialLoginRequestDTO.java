package org.example.sogone.auth.dto;

import org.example.sogone.auth.entity.type.Platform;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequestDTO(
	String token,
	String code,
	String nickname, // 애플은 token에 이름이 없고, 첫 로그인때만 클라이언트에게 이름을 줌
	@NotNull(message = "플랫폼을 입력하세요.") Platform platform
) {
	@AssertTrue(message = "token 또는 code가 필요합니다.")
	public boolean isCredentialPresent() {
		return (token != null && !token.isBlank()
			|| (code != null && !code.isBlank()));
	}
}
