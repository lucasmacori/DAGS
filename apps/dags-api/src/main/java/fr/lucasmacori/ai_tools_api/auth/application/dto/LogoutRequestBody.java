package fr.lucasmacori.ai_tools_api.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestBody(@NotBlank String refreshToken) {
}
