package fr.lucasmacori.ai_tools_api.chat.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationMessage(
		String messageId,
		String conversationId,
		ConversationMessageRole role,
		String content,
		List<WebSearchResult> sources,
		LocalDateTime createdAt) {
}
