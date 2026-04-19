package fr.lucasmacori.ai_tools_api.translation.domain.model;

public record TranslationRequest(String baseLanguage, String targetLanguage, String text) {
}
