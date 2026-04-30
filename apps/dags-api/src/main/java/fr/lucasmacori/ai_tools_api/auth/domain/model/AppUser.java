package fr.lucasmacori.ai_tools_api.auth.domain.model;

import java.time.LocalDateTime;

public record AppUser(
		String userId,
		String email,
		String passwordHash,
		boolean enabled,
		LocalDateTime createdAt) {
}
