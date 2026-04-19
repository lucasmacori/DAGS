package fr.lucasmacori.ai_tools_api.chat.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import reactor.core.publisher.Flux;

@Service
public class ChatApplicationService {

	private final ChatService chatService;

	public ChatApplicationService(ChatService chatService) {
		this.chatService = chatService;
	}

	public Flux<String> chat(ChatRequestBody request) {
		ChatRequest chatRequest = new ChatRequest(request.chatId(), request.message(), request.model());
		return chatService.chat(chatRequest);
	}

	public UUID generateChatId() {
		return chatService.generateChatId();
	}
}
