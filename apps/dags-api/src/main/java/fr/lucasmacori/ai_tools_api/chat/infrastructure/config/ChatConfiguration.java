package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import java.time.Duration;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.WebSearchClient;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.repository.InMemoryChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.repository.PgVectorChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.search.TavilyWebSearchClient;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(ChatPromptProperties.class)
@RequiredArgsConstructor
class ChatConfiguration {

	private static final int CHAT_DOCUMENT_EMBEDDING_DIMENSIONS = 768;

	private final IConversationRepository conversationRepository;
	private final IConversationHistoryRepository conversationHistoryRepository;

	@Bean
	ChatService chatService(
			ChatGenerator chatGenerator,
			ChatPromptProperties chatPromptProperties,
			IChatDocumentRepository chatDocumentRepository,
			WebSearchClient webSearchClient) {
		return new ChatService(
				chatGenerator,
				chatPromptProperties.defaultModel(),
				chatPromptProperties.system(),
				conversationRepository,
				conversationHistoryRepository,
				chatDocumentRepository,
				webSearchClient);
	}

	@Bean
	WebSearchClient webSearchClient(ChatPromptProperties chatPromptProperties, WebClient.Builder webClientBuilder) {
		if (!chatPromptProperties.webSearchEnabled()) {
			return query -> java.util.List.of();
		}

		String provider = chatPromptProperties.webSearchProvider().trim().toLowerCase();
		if (!"tavily".equals(provider)) {
			throw new IllegalStateException("Unsupported web search provider: " + chatPromptProperties.webSearchProvider());
		}

		String apiKey = chatPromptProperties.tavilyApiKey().trim();
		if (apiKey.isEmpty()) {
			return query -> java.util.List.of();
		}

		WebClient webClient = webClientBuilder
				.baseUrl(chatPromptProperties.tavilyBaseUrl())
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();

		return new TavilyWebSearchClient(
				webClient,
				Duration.ofSeconds(chatPromptProperties.tavilyTimeoutSeconds()),
				chatPromptProperties.tavilyMaxResults(),
				chatPromptProperties.tavilySearchDepth());
	}

	@Bean
	ChatMemoryRepository chatMemoryRepository(ChatPromptProperties chatPromptProperties, JdbcTemplate jdbcTemplate) {
		String provider = chatPromptProperties.memoryProvider().trim().toLowerCase();

		return switch (provider) {
			case "in-memory" -> new InMemoryChatMemoryRepository();
			case "postgres" -> JdbcChatMemoryRepository.builder()
					.jdbcTemplate(jdbcTemplate)
					.dialect(new PostgresChatMemoryRepositoryDialect())
					.build();
			default -> throw new IllegalStateException(
					"Unsupported chat memory provider: " + chatPromptProperties.memoryProvider());
		};
	}

	@Bean
	ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository, ChatPromptProperties chatPromptProperties) {
		return MessageWindowChatMemory.builder()
				.chatMemoryRepository(chatMemoryRepository)
				.maxMessages(chatPromptProperties.maxMemoryMessages())
				.build();
	}

	@Bean
	IChatDocumentRepository chatDocumentRepository(
			ChatPromptProperties chatPromptProperties,
			InMemoryChatDocumentRepository inMemoryChatDocumentRepository,
			ObjectProvider<PgVectorChatDocumentRepository> pgVectorChatDocumentRepositoryProvider) {
		String provider = chatPromptProperties.documentProvider().trim().toLowerCase();

		return switch (provider) {
			case "in-memory" -> inMemoryChatDocumentRepository;
			case "pgvector" -> {
				PgVectorChatDocumentRepository repository = pgVectorChatDocumentRepositoryProvider.getIfAvailable();
				if (repository == null) {
					throw new IllegalStateException("PGVector chat document repository is not available");
				}
				yield repository;
			}
			default -> throw new IllegalStateException(
					"Unsupported chat document provider: " + chatPromptProperties.documentProvider());
		};
	}

	@Bean
	@ConditionalOnProperty(prefix = "chat.documents", name = "provider", havingValue = "pgvector")
	VectorStore chatDocumentVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
		return PgVectorStore.builder(jdbcTemplate, embeddingModel)
				.dimensions(CHAT_DOCUMENT_EMBEDDING_DIMENSIONS)
				.vectorTableName("chat_document_vector_store")
				.schemaName("public")
				.initializeSchema(true)
				.build();
	}
}
