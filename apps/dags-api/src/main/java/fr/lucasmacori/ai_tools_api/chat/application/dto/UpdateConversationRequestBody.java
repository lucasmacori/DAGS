package fr.lucasmacori.ai_tools_api.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateConversationRequestBody(@JsonProperty("name") String name) {

	public boolean hasUpdates() {
		return name != null;
	}
}
