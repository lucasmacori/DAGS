package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat")
public record ChatPromptProperties(String defaultModel, Prompt prompt, Memory memory, Documents documents) {

	private static final String DEFAULT_MEMORY_PROVIDER = "in-memory";
	private static final String DEFAULT_DOCUMENT_PROVIDER = "in-memory";

	private static final int DEFAULT_MAX_MESSAGES = 20;
	private static final int DEFAULT_MAX_DOCUMENT_CHARACTERS = 200_000;
	private static final long DEFAULT_MAX_DOCUMENT_FILE_SIZE_BYTES = 10L * 1024L * 1024L;

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

	public String documentProvider() {
		if (documents == null || documents.provider() == null || documents.provider().isBlank()) {
			return DEFAULT_DOCUMENT_PROVIDER;
		}

		return documents.provider();
	}

	public int maxDocumentCharacters() {
		if (documents == null || documents.maxCharacters() == null) {
			return DEFAULT_MAX_DOCUMENT_CHARACTERS;
		}

		return documents.maxCharacters();
	}

	public long maxDocumentFileSizeBytes() {
		if (documents == null || documents.maxFileSizeBytes() == null) {
			return DEFAULT_MAX_DOCUMENT_FILE_SIZE_BYTES;
		}

		return documents.maxFileSizeBytes();
	}

	public record Prompt(String system) {
	}

	public record Memory(String provider, Integer maxMessages) {
	}

	public record Documents(String provider, Integer maxCharacters, Long maxFileSizeBytes) {
	}
}
