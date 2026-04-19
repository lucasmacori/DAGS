package fr.lucasmacori.ai_tools_api.chat.domain.model;

import java.time.LocalDateTime;

public record ConversationMessage(
		String messageId,
		String conversationId,
		ConversationMessageRole role,
		String content,
		LocalDateTime createdAt) {
}
