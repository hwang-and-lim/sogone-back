package org.example.sogone.auth.dto;

import jakarta.validation.constraints.NotNull;

public record EmailSendRequestDTO(
	@NotNull String email
) {
}
