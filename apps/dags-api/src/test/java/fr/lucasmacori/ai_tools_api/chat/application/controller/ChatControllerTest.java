package fr.lucasmacori.ai_tools_api.chat.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import fr.lucasmacori.ai_tools_api.chat.application.service.ChatApplicationService;
import fr.lucasmacori.ai_tools_api.infrastructure.security.SecurityConfiguration;
import reactor.core.publisher.Flux;

@WebFluxTest(ChatController.class)
@Import(SecurityConfiguration.class)
class ChatControllerTest {

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
	void chatReturnsUnauthorizedWithoutAuthentication() {
		webTestClient.post()
				.uri("/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"chat_id\": \"123\",
						  \"message\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void chatStreamsAssistantResponse() {
		when(applicationService.chat(any())).thenReturn(Flux.just("Hello", " there"));

		FluxExchangeResult<String> result = authenticatedClient().post()
				.uri("/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"chat_id\": \"123\",
						  \"message\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
				.returnResult(String.class);

		List<String> chunks = result.getResponseBody().collectList().block();

		if (chunks == null) {
			throw new AssertionError("Expected streamed chat response");
		}

		String response = String.join("", chunks);

		if (!response.contains("Hello") || !response.contains("there")) {
			throw new AssertionError("Expected streamed chunks but got: " + chunks);
		}

		verify(applicationService).chat(any());
	}

	@Test
	void chatReturnsBadRequestWhenChatIdIsMissing() {
		authenticatedClient().post()
				.uri("/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"message\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void chatReturnsBadRequestWhenChatIdIsBlank() {
		authenticatedClient().post()
				.uri("/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"chat_id\": \"   \",
						  \"message\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void chatReturnsBadRequestWhenMessageIsMissing() {
		authenticatedClient().post()
				.uri("/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"chat_id\": \"123\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void chatReturnsBadRequestWhenMessageIsBlank() {
		authenticatedClient().post()
				.uri("/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"chat_id\": \"123\",
						  \"message\": \"   \"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void generateChatReturnsUnauthorizedWithoutAuthentication() {
		webTestClient.post()
				.uri("/conversation")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void generateChatReturnsChatIdAsJson() {
		fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation conversation = new fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation("930d7b72-4cc0-450d-9f88-a8c3abef3b87", "test", null);
		when(applicationService.createConversation(any())).thenReturn(conversation);

		authenticatedClient().post()
				.uri("/conversation")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"name\": \"test\"}")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.chat_id").isEqualTo("930d7b72-4cc0-450d-9f88-a8c3abef3b87");
	}
}
