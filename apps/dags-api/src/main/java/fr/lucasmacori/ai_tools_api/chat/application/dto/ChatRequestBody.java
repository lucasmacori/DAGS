package fr.lucasmacori.ai_tools_api.chat.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestBody(
		@JsonProperty("chat_id") @NotBlank String chatId,
		@JsonProperty("message") @NotBlank String message,
		@JsonProperty("model") String model,
		@JsonProperty("document_ids") List<String> documentIds) {
}
