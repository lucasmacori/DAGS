package fr.lucasmacori.ai_tools_api.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestBody(
		@JsonProperty("chat_id") @NotBlank String chatId,
		@JsonProperty("message") @NotBlank String message,
		@JsonProperty("model") String model) {
}
