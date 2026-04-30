package fr.lucasmacori.ai_tools_api.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestBody(
		@NotBlank @Email String email,
		@NotBlank String password) {
}
