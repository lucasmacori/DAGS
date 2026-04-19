package fr.lucasmacori.ai_tools_api.chat.domain.service;

import java.util.UUID;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

public class ChatService {

	private final ChatGenerator chatGenerator;
	private final String defaultModel;
	private final String systemPrompt;

	public ChatService(ChatGenerator chatGenerator, String defaultModel, String systemPrompt) {
		this.chatGenerator = chatGenerator;
		this.defaultModel = defaultModel;
		this.systemPrompt = systemPrompt;
	}

	public Flux<String> chat(ChatRequest request) {
		return chat(request, systemPrompt);
	}

	public Flux<String> chat(ChatRequest request, String systemPrompt) {
		return chatGenerator.stream(request.chatId(), systemPrompt, request.message(), resolveModel(request));
	}

	public UUID generateChatId() {
		return UUID.randomUUID();
	}

	private String resolveModel(ChatRequest request) {
		if (request.model() != null && !request.model().isBlank()) {
			return request.model();
		}

		return defaultModel;
	}
}
