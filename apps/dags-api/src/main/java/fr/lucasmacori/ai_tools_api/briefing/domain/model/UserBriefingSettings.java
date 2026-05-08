package fr.lucasmacori.ai_tools_api.briefing.domain.model;

import java.time.LocalDateTime;

public record UserBriefingSettings(
		String userId,
		boolean enabled,
		String frequency,
		String generationTime,
		String systemPrompt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
