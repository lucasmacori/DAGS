package fr.lucasmacori.ai_tools_api.briefing.domain.model;

import java.time.LocalDateTime;

public record Source(
		String sourceId,
		SourceType type,
		String title,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime articleReadAt,
		String userId,
		String articleContent,
		LocalDateTime summarizedAt) {

	public Source(
			String sourceId,
			SourceType type,
			String title,
			String content,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime articleReadAt) {
		this(sourceId, type, title, content, createdAt, updatedAt, articleReadAt, null, null, null);
	}
}
