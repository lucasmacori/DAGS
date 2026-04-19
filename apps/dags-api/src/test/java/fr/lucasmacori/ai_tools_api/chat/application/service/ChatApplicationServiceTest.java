package fr.lucasmacori.ai_tools_api.chat.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.mock;

class ChatApplicationServiceTest {

	@Test
	void chatDelegatesToDomainService() {
		ChatGenerator generator = (chatId, systemPrompt, userMessage, model) -> Flux.just("Hello there");
		fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository repo = mock(fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository.class);
		ChatApplicationService applicationService = new ChatApplicationService(new ChatService(generator, "default-model", "System prompt", repo));

		String response = applicationService.chat(new ChatRequestBody("chat-1", "Hello", null))
				.collectList()
				.block()
				.getFirst();

		assertEquals("Hello there", response);
	}

	@Test
	void generateChatIdReturnsUuid() {
		ChatGenerator generator = (chatId, systemPrompt, userMessage, model) -> Flux.just("Hello there");
		fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository repo = mock(fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository.class);
		org.mockito.Mockito.when(repo.createConversation("test")).thenReturn(new fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation("930d7b72-4cc0-450d-9f88-a8c3abef3b87", "test", null));
		ChatApplicationService applicationService = new ChatApplicationService(new ChatService(generator, "default-model", "System prompt", repo));

		assertNotNull(applicationService.createConversation("test"));
	}
}
