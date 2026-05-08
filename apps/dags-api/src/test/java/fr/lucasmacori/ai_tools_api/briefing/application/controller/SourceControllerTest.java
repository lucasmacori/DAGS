package fr.lucasmacori.ai_tools_api.briefing.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.briefing.application.service.ArticleReadApplicationService;
import fr.lucasmacori.ai_tools_api.briefing.application.service.RssFeedReadApplicationService;
import fr.lucasmacori.ai_tools_api.briefing.application.service.SourceApplicationService;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.infrastructure.security.SecurityConfiguration;

@WebFluxTest(SourceController.class)
@Import(SecurityConfiguration.class)
class SourceControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private JwtEncoder jwtEncoder;

	@MockitoBean
	private SourceApplicationService applicationService;

	@MockitoBean
	private RssFeedReadApplicationService rssFeedReadApplicationService;

	@MockitoBean
	private ArticleReadApplicationService articleReadApplicationService;

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
	void getSourcesReturnsList() {
		when(applicationService.getSources("user-1")).thenReturn(List.of(
				new Source("source-1", SourceType.PLAIN_TEXT, "Notes", "hello", LocalDateTime.parse("2026-04-19T23:00:00"), LocalDateTime.parse("2026-04-19T23:00:00"), null),
				new Source("source-2", SourceType.RSS_FEED, "Feed", "https://example.com/rss", LocalDateTime.parse("2026-04-19T23:05:00"), LocalDateTime.parse("2026-04-19T23:05:00"), null)));

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
	void readRssFeedsReturnsAccepted() {
		authenticatedClient().post()
				.uri("/api/v1/source/rss/read")
				.exchange()
				.expectStatus().isAccepted();
	}

	@Test
	void readArticlesReturnsAccepted() {
		authenticatedClient().post()
				.uri("/api/v1/source/articles/read")
				.exchange()
				.expectStatus().isAccepted();
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
		Source source = new Source("source-1", SourceType.PLAIN_TEXT, "Notes", "hello", LocalDateTime.parse("2026-04-19T23:00:00"), LocalDateTime.parse("2026-04-19T23:00:00"), null);
		when(applicationService.createSource(any(), eq("user-1"))).thenReturn(source);

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
		Source source = new Source("source-1", SourceType.ARTICLE_URL, "Updated", "https://example.com", LocalDateTime.parse("2026-04-19T23:00:00"), LocalDateTime.parse("2026-04-19T23:10:00"), null);
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
