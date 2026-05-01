package fr.lucasmacori.ai_tools_api.chat.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.chat.application.service.ChatDocumentApplicationService;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.infrastructure.security.SecurityConfiguration;
import reactor.core.publisher.Mono;

@WebFluxTest(ChatDocumentController.class)
@Import(SecurityConfiguration.class)
class ChatDocumentControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private JwtEncoder jwtEncoder;

	@MockitoBean
	private ChatDocumentApplicationService applicationService;

	private WebTestClient authenticatedClient() {
		return webTestClient.mutate()
				.defaultHeaders(headers -> headers.setBearerAuth(createAccessToken()))
				.build();
	}

	private String createAccessToken() {
		Instant now = Instant.now();
		return jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder()
						.subject("user-1")
						.claim("email", "user@example.com")
						.issuedAt(now)
						.expiresAt(now.plusSeconds(300))
						.build()))
				.getTokenValue();
	}

	@Test
	void uploadDocumentsReturnsUploadedDocuments() {
		ChatDocument document = new ChatDocument("doc-1", "notes.txt", "text/plain", "hello world", LocalDateTime.now());
		when(applicationService.uploadDocuments(any())).thenReturn(Mono.just(List.of(document)));

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
		when(applicationService.deleteDocument("doc-1")).thenReturn(Mono.empty());

		authenticatedClient().delete()
				.uri("/api/v1/chat/document/doc-1")
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteDocumentReturnsNotFoundWhenMissing() {
		when(applicationService.deleteDocument("doc-1"))
				.thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found")));

		authenticatedClient().delete()
				.uri("/api/v1/chat/document/doc-1")
				.exchange()
				.expectStatus().isNotFound();
	}
}
