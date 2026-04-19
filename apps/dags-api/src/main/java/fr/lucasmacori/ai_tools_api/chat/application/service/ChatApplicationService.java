package fr.lucasmacori.ai_tools_api.chat.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatApplicationService {
	private final ChatService chatService;

	public Flux<String> chat(ChatRequestBody request) {
		ChatRequest chatRequest = new ChatRequest(request.chatId(), request.message(), request.model());
		return chatService.chat(chatRequest);
	}

	public List<Conversation> getConversations() {
		return chatService.getConversations();
	}

	public Conversation createConversation(final String name) {
		return chatService.createConversation(name);
	}

	public ConversationHistoryPage getConversationHistory(String conversationId, int page) {
		return chatService.getConversationHistory(conversationId, page);
	}
}
