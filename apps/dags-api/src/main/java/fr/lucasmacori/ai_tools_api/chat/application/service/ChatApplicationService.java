package fr.lucasmacori.ai_tools_api.chat.application.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.application.dto.UpdateConversationRequestBody;
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

	public Flux<ServerSentEvent<?>> chat(ChatRequestBody request) {
		ChatRequest chatRequest = new ChatRequest(
				request.chatId(),
				request.message(),
				request.model(),
				request.documentIds(),
				request.webSearch());
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

	public Conversation updateConversation(String conversationId, UpdateConversationRequestBody request) {
		if (!request.hasUpdates()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
		}

		try {
			return chatService.updateConversation(conversationId, request.name())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public void deleteConversation(String conversationId) {
		try {
			if (!chatService.deleteConversation(conversationId)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
			}
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}
}
