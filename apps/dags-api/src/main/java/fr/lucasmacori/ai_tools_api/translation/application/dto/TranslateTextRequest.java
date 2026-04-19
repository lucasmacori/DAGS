package fr.lucasmacori.ai_tools_api.translation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record TranslateTextRequest(
		@JsonProperty("base_language") String baseLanguage,
		@JsonProperty("target_language") @NotBlank String targetLanguage,
		@JsonProperty("text") @NotBlank String text) {
}
