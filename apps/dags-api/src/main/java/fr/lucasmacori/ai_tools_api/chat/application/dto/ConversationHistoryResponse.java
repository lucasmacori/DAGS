package fr.lucasmacori.ai_tools_api.chat.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;

public record ConversationHistoryResponse(
		@JsonProperty("page") int page,
		@JsonProperty("size") int size,
		@JsonProperty("messages") List<ConversationHistoryItemResponse> messages) {

	public static ConversationHistoryResponse from(ConversationHistoryPage historyPage) {
		return new ConversationHistoryResponse(
				historyPage.page(),
				historyPage.size(),
				historyPage.messages().stream().map(ConversationHistoryItemResponse::from).toList());
	}
}
