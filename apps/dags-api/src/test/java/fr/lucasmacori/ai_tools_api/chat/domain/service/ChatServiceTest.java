package fr.lucasmacori.ai_tools_api.chat.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

class ChatServiceTest {

	@Test
	void chatUsesDefaultModelWhenRequestModelIsMissing() {
		CapturingChatGenerator generator = new CapturingChatGenerator();
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		IChatDocumentRepository documentRepository = mock(IChatDocumentRepository.class);
		ChatService service = new ChatService(generator, "default-model", "System prompt", conversationRepository, historyRepository, documentRepository);

		List<String> response = service.chat(new ChatRequest(UUID.randomUUID().toString(), "Hello", null, null))
				.collectList()
				.block();

		assertEquals(List.of("Hi"), response);
		assertEquals("default-model", generator.model);
		assertEquals("System prompt", generator.systemPrompt);
		assertTrue(generator.chatId != null && !generator.chatId.isBlank());
		assertEquals("Hello", generator.userMessage);
		verify(historyRepository).addMessage(generator.chatId, ConversationMessageRole.USER, "Hello");
		verify(historyRepository).addMessage(generator.chatId, ConversationMessageRole.ASSISTANT, "Hi");
	}

	@Test
	void chatUsesRequestModelWhenProvided() {
		CapturingChatGenerator generator = new CapturingChatGenerator();
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		IChatDocumentRepository documentRepository = mock(IChatDocumentRepository.class);
		ChatService service = new ChatService(generator, "default-model", "System prompt", conversationRepository, historyRepository, documentRepository);

		service.chat(new ChatRequest("chat-1", "Hello", "mistral", null))
				.collectList()
				.block();

		assertEquals("chat-1", generator.chatId);
		assertEquals("Hello", generator.userMessage);
		assertEquals("mistral", generator.model);
	}

	@Test
	void chatIncludesAttachedDocumentsInPrompt() {
		CapturingChatGenerator generator = new CapturingChatGenerator();
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		IChatDocumentRepository documentRepository = mock(IChatDocumentRepository.class);
		when(documentRepository.findAllByIds(List.of("doc-1")))
				.thenReturn(List.of(new ChatDocument("doc-1", "notes.txt", "text/plain", "Document body", LocalDateTime.now())));
		ChatService service = new ChatService(generator, "default-model", "System prompt", conversationRepository, historyRepository, documentRepository);

		service.chat(new ChatRequest("chat-1", "Summarize this", null, List.of("doc-1")))
				.collectList()
				.block();

		assertTrue(generator.userMessage.contains("Attached documents:"));
		assertTrue(generator.userMessage.contains("notes.txt"));
		assertTrue(generator.userMessage.contains("Document body"));
	}

	@Test
	void getConversationHistoryUsesFirstPageWithTwentyMessages() {
		IConversationRepository conversationRepository = mock(IConversationRepository.class);
		IConversationHistoryRepository historyRepository = mock(IConversationHistoryRepository.class);
		IChatDocumentRepository documentRepository = mock(IChatDocumentRepository.class);
		ConversationHistoryPage historyPage = new ConversationHistoryPage(
				0,
				20,
				List.of(new ConversationMessage("message-1", "chat-1", ConversationMessageRole.USER, "Hello", LocalDateTime.now())));
		when(historyRepository.getConversationHistory("chat-1", 0, 20)).thenReturn(historyPage);
		ChatService service = new ChatService((chatId, systemPrompt, userMessage, model) -> Flux.just("Hi"), "default-model", "System prompt", conversationRepository, historyRepository, documentRepository);

		ConversationHistoryPage result = service.getConversationHistory("chat-1", 0);

		assertEquals(historyPage, result);
		verify(historyRepository).getConversationHistory("chat-1", 0, 20);
	}

	private static final class CapturingChatGenerator implements ChatGenerator {

		private String chatId;
		private String systemPrompt;
		private String userMessage;
		private String model;

		@Override
		public Flux<String> stream(String chatId, String systemPrompt, String userMessage, String model) {
			this.chatId = chatId;
			this.systemPrompt = systemPrompt;
			this.userMessage = userMessage;
			this.model = model;
			return Flux.just("Hi");
		}
	}
}
