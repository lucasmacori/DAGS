package fr.lucasmacori.ai_tools_api.chat.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

class ChatServiceTest {

	@Test
	void chatUsesDefaultModelWhenRequestModelIsMissing() {
		CapturingChatGenerator generator = new CapturingChatGenerator();
		ChatService service = new ChatService(generator, "default-model", "System prompt");

		List<String> response = service.chat(new ChatRequest(UUID.randomUUID().toString(), "Hello", null))
				.collectList()
				.block();

		assertEquals(List.of("Hi"), response);
		assertEquals("default-model", generator.model);
		assertEquals("System prompt", generator.systemPrompt);
		assertTrue(generator.chatId != null && !generator.chatId.isBlank());
		assertEquals("Hello", generator.userMessage);
	}

	@Test
	void chatUsesRequestModelWhenProvided() {
		CapturingChatGenerator generator = new CapturingChatGenerator();
		ChatService service = new ChatService(generator, "default-model", "System prompt");

		service.chat(new ChatRequest("chat-1", "Hello", "mistral"))
				.collectList()
				.block();

		assertEquals("chat-1", generator.chatId);
		assertEquals("Hello", generator.userMessage);
		assertEquals("mistral", generator.model);
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
