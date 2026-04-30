package fr.lucasmacori.ai_tools_api.auth.domain.model;

import java.time.LocalDateTime;

public record RefreshTokenRecord(
		String tokenId,
		String userId,
		String tokenHash,
		LocalDateTime expiresAt,
		LocalDateTime revokedAt,
		LocalDateTime createdAt) {
	public boolean isActiveAt(LocalDateTime instant) {
		return revokedAt == null && expiresAt.isAfter(instant);
	}
}
