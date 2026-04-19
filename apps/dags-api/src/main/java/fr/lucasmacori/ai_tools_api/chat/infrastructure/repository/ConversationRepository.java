package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.entity.ConversationEntity;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.repository.jdbc.ConversationJDBCRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConversationRepository implements IConversationRepository {
	private final ConversationJDBCRepository conversationJDBCRepository;

	@Override
	public List<Conversation> getConversations() {
		return conversationJDBCRepository.findAll().stream()
				.map(ConversationEntity::toConversation)
				.toList();
	}

	@Override
	public Conversation createConversation(String name) {
		return conversationJDBCRepository.save(ConversationEntity.fromName(name))
				.toConversation();
	}
}
