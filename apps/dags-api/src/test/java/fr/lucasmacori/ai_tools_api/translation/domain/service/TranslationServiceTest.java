package fr.lucasmacori.ai_tools_api.translation.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.translation.domain.model.TranslationRequest;
import fr.lucasmacori.ai_tools_api.translation.domain.spi.TranslationGenerator;
import reactor.core.publisher.Flux;

class TranslationServiceTest {

	@Test
	void translateBuildsPromptWithBaseLanguage() {
		CapturingTranslationGenerator translationGenerator = new CapturingTranslationGenerator();
		TranslationService translationService = new TranslationService(translationGenerator);

		List<String> chunks = translationService.translate(new TranslationRequest("en", "fr", "Hello"))
				.collectList()
				.block();

		assertEquals(List.of("Translated text"), chunks);
		assertTrue(translationGenerator.userPrompt.contains("Translate the following text from en to fr."));
		assertTrue(translationGenerator.userPrompt.contains("Hello"));
	}

	@Test
	void translateBuildsPromptWithoutBaseLanguage() {
		CapturingTranslationGenerator translationGenerator = new CapturingTranslationGenerator();
		TranslationService translationService = new TranslationService(translationGenerator);

		translationService.translate(new TranslationRequest(null, "fr", "Bonjour"))
				.collectList()
				.block();

		assertTrue(translationGenerator.userPrompt.contains("Translate the following text to fr."));
		assertTrue(translationGenerator.userPrompt.contains("Auto-detect the source language."));
		assertTrue(translationGenerator.userPrompt.contains("Bonjour"));
	}

	private static final class CapturingTranslationGenerator implements TranslationGenerator {

		private String userPrompt;

		@Override
		public Flux<String> stream(String userPrompt) {
			this.userPrompt = userPrompt;
			return Flux.just("Translated text");
		}
	}
}
