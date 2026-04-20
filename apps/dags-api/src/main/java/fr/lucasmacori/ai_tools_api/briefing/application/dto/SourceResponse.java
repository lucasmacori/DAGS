package fr.lucasmacori.ai_tools_api.briefing.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;

public record SourceResponse(
		@JsonProperty("source_id") String sourceId,
		@JsonProperty("type") SourceType type,
		@JsonProperty("title") String title,
		@JsonProperty("content") String content,
		@JsonProperty("created_at") LocalDateTime createdAt,
		@JsonProperty("updated_at") LocalDateTime updatedAt) {

	public static SourceResponse from(Source source) {
		return new SourceResponse(
				source.sourceId(),
				source.type(),
				source.title(),
				source.content(),
				source.createdAt(),
				source.updatedAt());
	}
}
