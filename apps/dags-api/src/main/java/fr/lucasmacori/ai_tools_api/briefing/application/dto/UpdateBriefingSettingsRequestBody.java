package fr.lucasmacori.ai_tools_api.briefing.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateBriefingSettingsRequestBody(
		@JsonProperty("enabled") Boolean enabled,
		@JsonProperty("frequency") String frequency,
		@JsonProperty("generation_time") String generationTime,
		@JsonProperty("system_prompt") String systemPrompt) {

	public boolean hasUpdates() {
		return enabled != null || frequency != null || generationTime != null || systemPrompt != null;
	}
}
