package fr.lucasmacori.ai_tools_api.chat.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;

@Repository
public interface IConversationRepository {
	Conversation createConversation(final String name);
	List<Conversation> getConversations();
	Optional<Conversation> findById(String conversationId);
	Conversation updateConversation(Conversation conversation);
}
