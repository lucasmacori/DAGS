package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat")
public record ChatPromptProperties(String defaultModel, Prompt prompt) {

	public String system() {
		return prompt.system();
	}

	public record Prompt(String system) {
	}
}
