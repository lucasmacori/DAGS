package fr.lucasmacori.ai_tools_api.briefing.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;

public record UpdateSourceRequestBody(
		@JsonProperty("type") SourceType type,
		@JsonProperty("title") String title,
		@JsonProperty("content") String content) {

	public boolean hasUpdates() {
		return type != null || title != null || content != null;
	}
}
