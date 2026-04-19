package fr.lucasmacori.ai_tools_api.translation.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import fr.lucasmacori.ai_tools_api.translation.application.service.TranslateTextApplicationService;
import fr.lucasmacori.ai_tools_api.translation.domain.model.Translation;

@WebFluxTest(TranslationController.class)
class TranslationControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private TranslateTextApplicationService applicationService;

	@Test
	void translateStreamsTranslatedText() {
		when(applicationService.translate(any())).thenReturn(new Translation("Translated text"));

		FluxExchangeResult<String> result = webTestClient.post()
				.uri("/translate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"base_language\": \"en\",
						  \"target_language\": \"fr\",
						  \"text\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
				.returnResult(String.class);

		List<String> translatedChunks = result.getResponseBody().collectList().block();

		if (translatedChunks == null) {
			throw new AssertionError("Expected streamed translated text");
		}

		if (!translatedChunks.equals(List.of("Translated text"))) {
			throw new AssertionError("Expected a single translated text chunk but got: " + translatedChunks);
		}

		verify(applicationService).translate(any());
	}

	@Test
	void translateReturnsBadRequestWhenTargetLanguageIsMissing() {
		webTestClient.post()
				.uri("/translate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"text\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void translateReturnsBadRequestWhenTargetLanguageIsBlank() {
		webTestClient.post()
				.uri("/translate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"target_language\": \"  \" ,
						  \"text\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void translateReturnsBadRequestWhenTextIsMissing() {
		webTestClient.post()
				.uri("/translate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"target_language\": \"fr\"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void translateReturnsBadRequestWhenTextIsBlank() {
		webTestClient.post()
				.uri("/translate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"target_language\": \"fr\",
						  \"text\": \"   \"
						}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void translateAllowsMissingBaseLanguage() {
		when(applicationService.translate(any())).thenReturn(new Translation("Translated text"));

		webTestClient.post()
				.uri("/translate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{
						  \"target_language\": \"fr\",
						  \"text\": \"Hello\"
						}
						""")
				.exchange()
				.expectStatus().isOk();
	}
}
