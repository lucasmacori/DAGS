package fr.lucasmacori.ai_tools_api.translation.domain.service;

import fr.lucasmacori.ai_tools_api.translation.domain.model.Translation;
import fr.lucasmacori.ai_tools_api.translation.domain.model.TranslationRequest;

public class TranslationService {

	public Translation translate(TranslationRequest request) {
		return new Translation("Translated text");
	}
}
