package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat")
public record ChatPromptProperties(String defaultModel, Prompt prompt, Memory memory) {

	private static final String DEFAULT_MEMORY_PROVIDER = "in-memory";

	private static final int DEFAULT_MAX_MESSAGES = 20;

	public String system() {
		return prompt.system();
	}

	public String memoryProvider() {
		if (memory == null || memory.provider() == null || memory.provider().isBlank()) {
			return DEFAULT_MEMORY_PROVIDER;
		}

		return memory.provider();
	}

	public int maxMemoryMessages() {
		if (memory == null || memory.maxMessages() == null) {
			return DEFAULT_MAX_MESSAGES;
		}

		return memory.maxMessages();
	}

	public record Prompt(String system) {
	}

	public record Memory(String provider, Integer maxMessages) {
	}
}
