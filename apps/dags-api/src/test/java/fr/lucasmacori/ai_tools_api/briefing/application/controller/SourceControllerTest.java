package fr.lucasmacori.ai_tools_api.briefing.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.briefing.application.service.SourceApplicationService;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.infrastructure.security.SecurityConfiguration;

@WebFluxTest(SourceController.class)
@Import(SecurityConfiguration.class)
class SourceControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private SourceApplicationService applicationService;

	private WebTestClient authenticatedClient() {
		return webTestClient.mutate()
				.defaultHeaders(headers -> headers.setBasicAuth("ai", "completelylocal"))
				.build();
	}

	@Test
	void getSourcesReturnsList() {
		when(applicationService.getSources()).thenReturn(List.of(
				new Source("source-1", SourceType.PLAIN_TEXT, "Notes", "hello", LocalDateTime.parse("2026-04-19T23:00:00"), LocalDateTime.parse("2026-04-19T23:00:00")),
				new Source("source-2", SourceType.RSS_FEED, "Feed", "https://example.com/rss", LocalDateTime.parse("2026-04-19T23:05:00"), LocalDateTime.parse("2026-04-19T23:05:00"))));

		authenticatedClient().get()
				.uri("/api/v1/source")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].source_id").isEqualTo("source-1")
				.jsonPath("$[0].type").isEqualTo("PLAIN_TEXT")
				.jsonPath("$[1].source_id").isEqualTo("source-2")
				.jsonPath("$[1].type").isEqualTo("RSS_FEED");
	}

	@Test
	void createSourceReturnsUnauthorizedWithoutAuthentication() {
		webTestClient.post()
				.uri("/api/v1/source")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"type\": \"PLAIN_TEXT\",
						  \"content\": \"hello\"
						}
						""")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void createSourceReturnsCreatedSource() {
		Source source = new Source("source-1", SourceType.PLAIN_TEXT, "Notes", "hello", LocalDateTime.parse("2026-04-19T23:00:00"), LocalDateTime.parse("2026-04-19T23:00:00"));
		when(applicationService.createSource(any())).thenReturn(source);

		authenticatedClient().post()
				.uri("/api/v1/source")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"type\": \"PLAIN_TEXT\",
						  \"title\": \"Notes\",
						  \"content\": \"hello\"
						}
						""")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.source_id").isEqualTo("source-1")
				.jsonPath("$.type").isEqualTo("PLAIN_TEXT")
				.jsonPath("$.title").isEqualTo("Notes")
				.jsonPath("$.content").isEqualTo("hello");
	}

	@Test
	void createSourceReturnsBadRequestWhenTypeIsMissing() {
		authenticatedClient().post()
				.uri("/api/v1/source")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"content\": \"hello\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void patchSourceReturnsUpdatedSource() {
		Source source = new Source("source-1", SourceType.ARTICLE_URL, "Updated", "https://example.com", LocalDateTime.parse("2026-04-19T23:00:00"), LocalDateTime.parse("2026-04-19T23:10:00"));
		when(applicationService.updateSource(any(), any())).thenReturn(source);

		authenticatedClient().patch()
				.uri("/api/v1/source/source-1")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"type\": \"ARTICLE_URL\",
						  \"title\": \"Updated\",
						  \"content\": \"https://example.com\"
						}
						""")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.source_id").isEqualTo("source-1")
				.jsonPath("$.type").isEqualTo("ARTICLE_URL");
	}

	@Test
	void patchSourceReturnsBadRequestWhenBodyIsEmpty() {
		when(applicationService.updateSource(any(), any()))
				.thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided"));

		authenticatedClient().patch()
				.uri("/api/v1/source/source-1")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{}")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void deleteSourceReturnsNoContent() {
		authenticatedClient().delete()
				.uri("/api/v1/source/source-1")
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteSourceReturnsNotFoundWhenMissing() {
		org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Source not found"))
				.when(applicationService)
				.deleteSource("source-1");

		authenticatedClient().delete()
				.uri("/api/v1/source/source-1")
				.exchange()
				.expectStatus().isNotFound();
	}
}
