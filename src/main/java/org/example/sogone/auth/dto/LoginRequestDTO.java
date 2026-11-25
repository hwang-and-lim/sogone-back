package org.example.sogone.auth.dto;

import org.example.sogone.auth.entity.type.Platform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
	@NotBlank(message = "아이디를 입력하세요.")
	private String email;

	@NotBlank(message = "비밀번호를 입력하세요.")
	private String password;

	@NotNull(message = "플랫폼을 입력하세요.")
	private Platform platform;
}
