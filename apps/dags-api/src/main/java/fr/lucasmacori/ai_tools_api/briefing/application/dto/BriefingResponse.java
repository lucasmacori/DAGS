package fr.lucasmacori.ai_tools_api.briefing.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Briefing;

public record BriefingResponse(
		@JsonProperty("briefing_id") String briefingId,
		@JsonProperty("content") String content,
		@JsonProperty("article_count") int articleCount,
		@JsonProperty("created_at") LocalDateTime createdAt) {

	public static BriefingResponse from(Briefing briefing) {
		return new BriefingResponse(
				briefing.briefingId(),
				briefing.content(),
				briefing.articleCount(),
				briefing.createdAt());
	}
}
