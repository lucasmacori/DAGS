package fr.lucasmacori.ai_tools_api.translation.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "translation.prompt")
public record TranslationPromptProperties(String system) {
}
