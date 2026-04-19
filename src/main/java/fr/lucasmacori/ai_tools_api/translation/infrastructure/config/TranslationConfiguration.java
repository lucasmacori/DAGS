package fr.lucasmacori.ai_tools_api.translation.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import fr.lucasmacori.ai_tools_api.translation.domain.service.TranslationService;
import fr.lucasmacori.ai_tools_api.translation.domain.spi.TranslationGenerator;

@Configuration
@EnableConfigurationProperties(TranslationPromptProperties.class)
class TranslationConfiguration {

	@Bean
	TranslationService translationService(TranslationGenerator translationGenerator) {
		return new TranslationService(translationGenerator);
	}
}
