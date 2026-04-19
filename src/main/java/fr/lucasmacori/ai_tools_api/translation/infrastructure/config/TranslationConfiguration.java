package fr.lucasmacori.ai_tools_api.translation.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.lucasmacori.ai_tools_api.translation.domain.service.TranslationService;

@Configuration
class TranslationConfiguration {

	@Bean
	TranslationService translationService() {
		return new TranslationService();
	}
}
