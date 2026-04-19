package fr.lucasmacori.ai_tools_api.chat.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

class ChatApplicationServiceTest {

	@Test
	void chatDelegatesToDomainService() {
		ChatGenerator generator = (chatId, systemPrompt, userMessage, model) -> Flux.just("Hello there");
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		ChatApplicationService applicationService = new ChatApplicationService(
				new ChatService(generator, "default-model", "System prompt", conversationRepository, historyRepository));

		String response = applicationService.chat(new ChatRequestBody("chat-1", "Hello", null))
				.collectList()
				.block()
				.getFirst();

		assertEquals("Hello there", response);
	}

	@Test
	void createConversationDelegatesToDomainService() {
		ChatGenerator generator = (chatId, systemPrompt, userMessage, model) -> Flux.just("Hello there");
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		when(conversationRepository.createConversation("test"))
				.thenReturn(new Conversation("930d7b72-4cc0-450d-9f88-a8c3abef3b87", "test", null));
		ChatApplicationService applicationService = new ChatApplicationService(
				new ChatService(generator, "default-model", "System prompt", conversationRepository, historyRepository));

		assertNotNull(applicationService.createConversation("test"));
	}

	@Test
	void getConversationHistoryDelegatesToDomainService() {
		ChatGenerator generator = (chatId, systemPrompt, userMessage, model) -> Flux.just("Hello there");
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		ConversationHistoryPage expected = new ConversationHistoryPage(
				0,
				20,
				List.of(new ConversationMessage("message-1", "chat-1", ConversationMessageRole.USER, "Hello", LocalDateTime.now())));
		when(historyRepository.getConversationHistory("chat-1", 0, 20)).thenReturn(expected);
		ChatApplicationService applicationService = new ChatApplicationService(
				new ChatService(generator, "default-model", "System prompt", conversationRepository, historyRepository));

		ConversationHistoryPage result = applicationService.getConversationHistory("chat-1", 0);

		assertEquals(expected, result);
	}
}
