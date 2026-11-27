package org.example.sogone.auth.dto;

import jakarta.validation.constraints.NotNull;

public record EmailVerifyCodeRequestDTO(
	@NotNull String email,
	@NotNull String code
) {
}
