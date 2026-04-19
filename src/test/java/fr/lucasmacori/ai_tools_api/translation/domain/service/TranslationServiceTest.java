package fr.lucasmacori.ai_tools_api.translation.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.translation.domain.model.Translation;
import fr.lucasmacori.ai_tools_api.translation.domain.model.TranslationRequest;

class TranslationServiceTest {

	@Test
	void translateReturnsHardcodedTranslatedText() {
		TranslationService translationService = new TranslationService();

		Translation translation = translationService.translate(new TranslationRequest("en", "fr", "Hello"));

		assertEquals("Translated text", translation.text());
	}
}
