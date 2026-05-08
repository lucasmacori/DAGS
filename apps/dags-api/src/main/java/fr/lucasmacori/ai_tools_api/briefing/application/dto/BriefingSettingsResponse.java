package fr.lucasmacori.ai_tools_api.briefing.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;

public record BriefingSettingsResponse(
		@JsonProperty("enabled") boolean enabled,
		@JsonProperty("frequency") String frequency,
		@JsonProperty("generation_time") String generationTime,
		@JsonProperty("system_prompt") String systemPrompt,
		@JsonProperty("created_at") LocalDateTime createdAt,
		@JsonProperty("updated_at") LocalDateTime updatedAt) {

	public static BriefingSettingsResponse from(UserBriefingSettings settings) {
		return new BriefingSettingsResponse(
				settings.enabled(),
				settings.frequency(),
				settings.generationTime(),
				settings.systemPrompt(),
				settings.createdAt(),
				settings.updatedAt());
	}
}
