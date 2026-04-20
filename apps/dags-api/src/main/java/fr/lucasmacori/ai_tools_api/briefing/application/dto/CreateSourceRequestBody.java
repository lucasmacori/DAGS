package fr.lucasmacori.ai_tools_api.briefing.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSourceRequestBody(
		@JsonProperty("type") @NotNull SourceType type,
		@JsonProperty("title") String title,
		@JsonProperty("content") @NotBlank String content) {
}
