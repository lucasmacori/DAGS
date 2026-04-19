package fr.lucasmacori.ai_tools_api.translation.application.service;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.translation.application.dto.TranslateTextRequest;
import fr.lucasmacori.ai_tools_api.translation.domain.service.TranslationService;
import fr.lucasmacori.ai_tools_api.translation.domain.spi.TranslationGenerator;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslateTextApplicationServiceTest {

	@Test
	void translateDelegatesToDomainService() {
		TranslationGenerator translationGenerator = userPrompt -> Flux.just("Translated text");
		TranslateTextApplicationService applicationService = new TranslateTextApplicationService(new TranslationService(translationGenerator));

		String translation = applicationService.translate(new TranslateTextRequest("en", "fr", "Hello"))
				.collectList()
				.block()
				.getFirst();

		assertEquals("Translated text", translation);
	}
}
