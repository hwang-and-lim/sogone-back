package org.example.sogone.auth.dto;

import org.example.sogone.auth.entity.type.Platform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {
	@NotBlank(message = "아이디를 입력하세요.")
	private String email;

	@NotBlank(message = "비밀번호를 입력하세요.")
	@Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
	private String password;

	@NotBlank(message = "닉네임을 입력하세요.")
	@Size(min = 2, max = 30)
	private String nickname;

	@NotNull(message = "플랫폼을 입력하세요.")
	private Platform platform;
}
