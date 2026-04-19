package fr.lucasmacori.ai_tools_api.chat.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

class ChatApplicationServiceTest {

	@Test
	void chatDelegatesToDomainService() {
		ChatGenerator generator = (systemPrompt, userPrompt, model) -> Flux.just("Hello there");
		ChatApplicationService applicationService = new ChatApplicationService(new ChatService(generator, "default-model", "System prompt"));

		String response = applicationService.chat(new ChatRequestBody("chat-1", "Hello", null))
				.collectList()
				.block()
				.getFirst();

		assertEquals("Hello there", response);
	}

	@Test
	void generateChatIdReturnsUuid() {
		ChatGenerator generator = (systemPrompt, userPrompt, model) -> Flux.just("Hello there");
		ChatApplicationService applicationService = new ChatApplicationService(new ChatService(generator, "default-model", "System prompt"));

		assertNotNull(applicationService.generateChatId());
	}
}
