package fr.lucasmacori.ai_tools_api.chat.domain.repository;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;

public interface IConversationHistoryRepository {
	void addMessage(String conversationId, ConversationMessageRole role, String content);

	ConversationHistoryPage getConversationHistory(String conversationId, int page, int size);

	void deleteConversationHistory(String conversationId);
}
