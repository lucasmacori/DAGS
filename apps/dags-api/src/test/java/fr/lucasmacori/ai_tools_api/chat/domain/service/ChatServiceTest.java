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
		assertTrue(generator.userPrompt.contains("Chat ID: "));
		assertTrue(generator.userPrompt.contains("User message:"));
		assertTrue(generator.userPrompt.contains("Hello"));
	}

	@Test
	void chatUsesRequestModelWhenProvided() {
		CapturingChatGenerator generator = new CapturingChatGenerator();
		ChatService service = new ChatService(generator, "default-model", "System prompt");

		service.chat(new ChatRequest("chat-1", "Hello", "mistral"))
				.collectList()
				.block();

		assertEquals("mistral", generator.model);
	}

	private static final class CapturingChatGenerator implements ChatGenerator {

		private String systemPrompt;
		private String userPrompt;
		private String model;

		@Override
		public Flux<String> stream(String systemPrompt, String userPrompt, String model) {
			this.systemPrompt = systemPrompt;
			this.userPrompt = userPrompt;
			this.model = model;
			return Flux.just("Hi");
		}
	}
}
