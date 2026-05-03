package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat")
public record ChatPromptProperties(String defaultModel, Prompt prompt, Memory memory, Documents documents, WebSearch webSearch) {

	private static final String DEFAULT_MEMORY_PROVIDER = "in-memory";
	private static final String DEFAULT_DOCUMENT_PROVIDER = "in-memory";
	private static final String DEFAULT_WEB_SEARCH_PROVIDER = "tavily";
	private static final String DEFAULT_TAVILY_BASE_URL = "https://api.tavily.com";
	private static final String DEFAULT_TAVILY_SEARCH_DEPTH = "basic";

	private static final int DEFAULT_MAX_MESSAGES = 20;
	private static final int DEFAULT_MAX_DOCUMENT_CHARACTERS = 200_000;
	private static final int DEFAULT_TAVILY_MAX_RESULTS = 5;
	private static final int DEFAULT_TAVILY_TIMEOUT_SECONDS = 10;
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

	public boolean webSearchEnabled() {
		return webSearch != null && Boolean.TRUE.equals(webSearch.enabled());
	}

	public String webSearchProvider() {
		if (webSearch == null || webSearch.provider() == null || webSearch.provider().isBlank()) {
			return DEFAULT_WEB_SEARCH_PROVIDER;
		}

		return webSearch.provider();
	}

	public String tavilyApiKey() {
		if (webSearch == null || webSearch.tavily() == null || webSearch.tavily().apiKey() == null) {
			return "";
		}

		return webSearch.tavily().apiKey();
	}

	public String tavilyBaseUrl() {
		if (webSearch == null || webSearch.tavily() == null || webSearch.tavily().baseUrl() == null || webSearch.tavily().baseUrl().isBlank()) {
			return DEFAULT_TAVILY_BASE_URL;
		}

		return webSearch.tavily().baseUrl();
	}

	public int tavilyMaxResults() {
		if (webSearch == null || webSearch.tavily() == null || webSearch.tavily().maxResults() == null) {
			return DEFAULT_TAVILY_MAX_RESULTS;
		}

		return webSearch.tavily().maxResults();
	}

	public String tavilySearchDepth() {
		if (webSearch == null || webSearch.tavily() == null || webSearch.tavily().searchDepth() == null || webSearch.tavily().searchDepth().isBlank()) {
			return DEFAULT_TAVILY_SEARCH_DEPTH;
		}

		return webSearch.tavily().searchDepth();
	}

	public int tavilyTimeoutSeconds() {
		if (webSearch == null || webSearch.tavily() == null || webSearch.tavily().timeoutSeconds() == null) {
			return DEFAULT_TAVILY_TIMEOUT_SECONDS;
		}

		return webSearch.tavily().timeoutSeconds();
	}

	public record Prompt(String system) {
	}

	public record Memory(String provider, Integer maxMessages) {
	}

	public record Documents(String provider, Integer maxCharacters, Long maxFileSizeBytes) {
	}

	public record WebSearch(Boolean enabled, String provider, Tavily tavily) {
	}

	public record Tavily(String apiKey, String baseUrl, Integer maxResults, String searchDepth, Integer timeoutSeconds) {
	}
}
