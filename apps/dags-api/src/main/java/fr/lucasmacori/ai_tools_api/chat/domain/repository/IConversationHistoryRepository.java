package fr.lucasmacori.ai_tools_api.chat.domain.repository;

import java.util.List;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.chat.domain.model.WebSearchResult;

public interface IConversationHistoryRepository {
	default void addMessage(String conversationId, ConversationMessageRole role, String content) {
		addMessage(conversationId, role, content, List.of());
	}

	void addMessage(String conversationId, ConversationMessageRole role, String content, List<WebSearchResult> sources);

	ConversationHistoryPage getConversationHistory(String conversationId, int page, int size);

	void deleteConversationHistory(String conversationId);
}
