package fr.lucasmacori.ai_tools_api.chat.domain.service;

import java.util.List;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ChatService {

	private final ChatGenerator chatGenerator;
	private final String defaultModel;
	private final String systemPrompt;

	private final IConversationRepository conversationRepository;

	public Flux<String> chat(ChatRequest request) {
		return chat(request, systemPrompt);
	}

	public Flux<String> chat(ChatRequest request, String systemPrompt) {
		return chatGenerator.stream(request.chatId(), systemPrompt, request.message(), resolveModel(request));
	}

	public List<Conversation> getConversations() {
		return conversationRepository.getConversations();
	}

	public Conversation createConversation(final String name) {
		return conversationRepository.createConversation(name);
	}

	private String resolveModel(ChatRequest request) {
		if (request.model() != null && !request.model().isBlank()) {
			return request.model();
		}

		return defaultModel;
	}
}
