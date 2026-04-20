package fr.lucasmacori.ai_tools_api.briefing.domain.model;

import java.time.LocalDateTime;

public record Source(
		String sourceId,
		SourceType type,
		String title,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime articleReadAt) {
}
