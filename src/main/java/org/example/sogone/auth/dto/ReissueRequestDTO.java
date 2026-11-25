package org.example.sogone.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequestDTO(
	@NotBlank String refreshToken
) {
}
