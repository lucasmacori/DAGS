package fr.lucasmacori.ai_tools_api.chat.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import fr.lucasmacori.ai_tools_api.chat.application.service.ChatApplicationService;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.infrastructure.security.SecurityConfiguration;

@WebFluxTest(ConversationController.class)
@Import(SecurityConfiguration.class)
class ConversationControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private ChatApplicationService applicationService;

	private WebTestClient authenticatedClient() {
		return webTestClient.mutate()
				.defaultHeaders(headers -> headers.setBasicAuth("ai", "completelylocal"))
				.build();
	}

	@Test
	void getConversationHistoryReturnsUnauthorizedWithoutAuthentication() {
		webTestClient.get()
				.uri("/api/v1/conversation/chat-1/history")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void getConversationHistoryReturnsPaginatedMessages() {
		ConversationHistoryPage historyPage = new ConversationHistoryPage(
				0,
				20,
				List.of(
						new ConversationMessage("message-2", "chat-1", ConversationMessageRole.ASSISTANT, "Hi", LocalDateTime.parse("2026-04-19T21:00:00")),
						new ConversationMessage("message-1", "chat-1", ConversationMessageRole.USER, "Hello", LocalDateTime.parse("2026-04-19T20:59:00"))));
		when(applicationService.getConversationHistory("chat-1", 0)).thenReturn(historyPage);

		authenticatedClient().get()
				.uri(uriBuilder -> uriBuilder.path("/api/v1/conversation/chat-1/history").queryParam("page", 0).build())
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.page").isEqualTo(0)
				.jsonPath("$.size").isEqualTo(20)
				.jsonPath("$.messages[0].message_id").isEqualTo("message-2")
				.jsonPath("$.messages[0].role").isEqualTo("ASSISTANT")
				.jsonPath("$.messages[1].message_id").isEqualTo("message-1")
				.jsonPath("$.messages[1].role").isEqualTo("USER");
	}

	@Test
	void createConversationReturnsChatIdAsJson() {
		Conversation conversation = new Conversation("930d7b72-4cc0-450d-9f88-a8c3abef3b87", "test", null);
		when(applicationService.createConversation(any())).thenReturn(conversation);

		authenticatedClient().post()
				.uri("/api/v1/conversation")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"name\": \"test\"}")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.chat_id").isEqualTo("930d7b72-4cc0-450d-9f88-a8c3abef3b87");
	}
}
