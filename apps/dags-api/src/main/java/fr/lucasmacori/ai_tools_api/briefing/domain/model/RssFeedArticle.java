package fr.lucasmacori.ai_tools_api.briefing.domain.model;

import java.time.LocalDateTime;

public record RssFeedArticle(
		String externalId,
		String title,
		String link,
		LocalDateTime publishedAt) {
}
