package fr.lucasmacori.ai_tools_api.translation.domain.service;

import fr.lucasmacori.ai_tools_api.translation.domain.model.TranslationRequest;
import fr.lucasmacori.ai_tools_api.translation.domain.spi.TranslationGenerator;
import reactor.core.publisher.Flux;

public class TranslationService {

	private final TranslationGenerator translationGenerator;

	public TranslationService(TranslationGenerator translationGenerator) {
		this.translationGenerator = translationGenerator;
	}

	public Flux<String> translate(TranslationRequest request) {
		return translationGenerator.stream(buildUserPrompt(request));
	}

	private String buildUserPrompt(TranslationRequest request) {
		if (hasBaseLanguage(request)) {
			return """
					Translate the following text from %s to %s.

					Text:
					%s
					""".formatted(request.baseLanguage(), request.targetLanguage(), request.text());
		}

		return """
				Translate the following text to %s.
				Auto-detect the source language.

				Text:
				%s
				""".formatted(request.targetLanguage(), request.text());
	}

	private boolean hasBaseLanguage(TranslationRequest request) {
		return request.baseLanguage() != null && !request.baseLanguage().isBlank();
	}
}
