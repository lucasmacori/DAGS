package fr.lucasmacori.ai_tools_api.chat.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;

public record ConversationHistoryItemResponse(
		@JsonProperty("message_id") String messageId,
		@JsonProperty("conversation_id") String conversationId,
		@JsonProperty("role") ConversationMessageRole role,
		@JsonProperty("content") String content,
		@JsonProperty("created_at") LocalDateTime createdAt) {

	public static ConversationHistoryItemResponse from(ConversationMessage message) {
		return new ConversationHistoryItemResponse(
				message.messageId(),
				message.conversationId(),
				message.role(),
				message.content(),
				message.createdAt());
	}
}
