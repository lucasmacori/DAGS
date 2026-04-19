package fr.lucasmacori.ai_tools_api.translation.application.service;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.translation.application.dto.TranslateTextRequest;
import fr.lucasmacori.ai_tools_api.translation.domain.model.Translation;
import fr.lucasmacori.ai_tools_api.translation.domain.model.TranslationRequest;
import fr.lucasmacori.ai_tools_api.translation.domain.service.TranslationService;

@Service
public class TranslateTextApplicationService {

	private final TranslationService translationService;

	public TranslateTextApplicationService(TranslationService translationService) {
		this.translationService = translationService;
	}

	public Translation translate(TranslateTextRequest request) {
		TranslationRequest translationRequest = new TranslationRequest(
				request.baseLanguage(),
				request.targetLanguage(),
				request.text());

		return translationService.translate(translationRequest);
	}
}
