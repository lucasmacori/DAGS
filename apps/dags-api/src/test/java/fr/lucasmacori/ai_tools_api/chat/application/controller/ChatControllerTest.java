package fr.lucasmacori.ai_tools_api.chat.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.chat.application.service.ChatApplicationService;
import fr.lucasmacori.ai_tools_api.chat.application.service.ChatDocumentApplicationService;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.infrastructure.security.SecurityConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest({ ChatController.class, ChatDocumentController.class })
@Import(SecurityConfiguration.class)
class ChatControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private ChatApplicationService applicationService;

	@MockitoBean
	private ChatDocumentApplicationService chatDocumentApplicationService;

	private WebTestClient authenticatedClient() {
		return webTestClient.mutate()
				.defaultHeaders(headers -> headers.setBasicAuth("ai", "completelylocal"))
				.build();
	}

	@Test
	void chatReturnsUnauthorizedWithoutAuthentication() {
		webTestClient.post()
				.uri("/api/v1/chat")
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
				.uri("/api/v1/chat")
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
	void uploadDocumentsReturnsUploadedDocuments() {
		ChatDocument document = new ChatDocument("doc-1", "notes.txt", "text/plain", "hello world", LocalDateTime.now());
		when(chatDocumentApplicationService.uploadDocuments(any())).thenReturn(Mono.just(List.of(document)));

		MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
		bodyBuilder.part("files", new ByteArrayResource("hello world".getBytes()) {
			@Override
			public String getFilename() {
				return "notes.txt";
			}
		}).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

		authenticatedClient().post()
				.uri("/api/v1/chat/document")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.bodyValue(bodyBuilder.build())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.documents[0].document_id").isEqualTo("doc-1")
				.jsonPath("$.documents[0].filename").isEqualTo("notes.txt")
				.jsonPath("$.documents[0].media_type").isEqualTo("text/plain");
	}

	@Test
	void deleteDocumentReturnsNoContent() {
		authenticatedClient().delete()
				.uri("/api/v1/chat/document/doc-1")
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteDocumentReturnsNotFoundWhenMissing() {
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"))
				.when(chatDocumentApplicationService)
				.deleteDocument("doc-1");

		authenticatedClient().delete()
				.uri("/api/v1/chat/document/doc-1")
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void chatReturnsBadRequestWhenChatIdIsMissing() {
		authenticatedClient().post()
				.uri("/api/v1/chat")
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
				.uri("/api/v1/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"chat_id\": \"   \" ,
						  \"message\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void chatReturnsBadRequestWhenMessageIsMissing() {
		authenticatedClient().post()
				.uri("/api/v1/chat")
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
				.uri("/api/v1/chat")
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
}
