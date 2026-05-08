package fr.lucasmacori.ai_tools_api.briefing.domain.model;

import java.time.LocalDateTime;

public record Briefing(
		String briefingId,
		String userId,
		String content,
		int articleCount,
		LocalDateTime createdAt) {
}
