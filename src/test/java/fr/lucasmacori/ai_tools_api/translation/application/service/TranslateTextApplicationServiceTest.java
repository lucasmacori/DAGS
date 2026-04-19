package fr.lucasmacori.ai_tools_api.translation.application.service;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.translation.application.dto.TranslateTextRequest;
import fr.lucasmacori.ai_tools_api.translation.domain.model.Translation;
import fr.lucasmacori.ai_tools_api.translation.domain.service.TranslationService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslateTextApplicationServiceTest {

	@Test
	void translateDelegatesToDomainService() {
		TranslateTextApplicationService applicationService = new TranslateTextApplicationService(new TranslationService());

		Translation translation = applicationService.translate(new TranslateTextRequest("en", "fr", "Hello"));

		assertEquals("Translated text", translation.text());
	}
}
