package fr.lucasmacori.ai_tools_api.chat.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.transaction.annotation.Transactional;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.chat.domain.model.WebSearchResult;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.WebSearchClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
public class ChatService {

	private static final int DEFAULT_HISTORY_PAGE_SIZE = 20;

	private final ChatGenerator chatGenerator;
	private final String defaultModel;
	private final String systemPrompt;
	private final IConversationRepository conversationRepository;
	private final IConversationHistoryRepository conversationHistoryRepository;
	private final IChatDocumentRepository chatDocumentRepository;
	private final WebSearchClient webSearchClient;

	public Flux<ServerSentEvent<?>> chat(ChatRequest request) {
		return chat(request, systemPrompt);
	}

	public Flux<ServerSentEvent<?>> chat(ChatRequest request, String systemPrompt) {
		return Mono.fromCallable(() -> buildUserMessage(request))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMapMany(promptedUserMessage -> {
					StringBuilder assistantResponse = new StringBuilder();
					Flux<ServerSentEvent<?>> generatedResponse = chatGenerator.stream(
							request.chatId(),
							systemPrompt,
							promptedUserMessage.content(),
							resolveModel(request))
							.doOnNext(assistantResponse::append)
							.map(chunk -> ServerSentEvent.builder(chunk).build());

					return persistMessage(request.chatId(), ConversationMessageRole.USER, request.message())
							.thenMany(generatedResponse.concatWith(Mono.defer(() -> persistAssistantMessage(request.chatId(), assistantResponse, promptedUserMessage.sources())
									.then(Mono.justOrEmpty(buildSourcesEvent(promptedUserMessage.sources()))))));
				});
	}

	public List<Conversation> getConversations() {
		return conversationRepository.getConversations();
	}

	public Conversation createConversation(final String name) {
		return conversationRepository.createConversation(name);
	}

	public ConversationHistoryPage getConversationHistory(String conversationId, int page) {
		return conversationHistoryRepository.getConversationHistory(conversationId, page, DEFAULT_HISTORY_PAGE_SIZE);
	}

	public Optional<Conversation> updateConversation(String conversationId, String name) {
		return conversationRepository.findById(conversationId)
				.map(existing -> {
					String nextName = name != null ? normalizeRequiredName(name) : existing.conversationName();
					Conversation updated = new Conversation(existing.conversationId(), nextName, existing.createdAt());
					return conversationRepository.updateConversation(updated);
				});
	}

	@Transactional
	public boolean deleteConversation(String conversationId) {
		if (conversationRepository.findById(conversationId).isEmpty()) {
			return false;
		}

		conversationHistoryRepository.deleteConversationHistory(conversationId);
		conversationRepository.deleteConversation(conversationId);
		return true;
	}

	private PromptedUserMessage buildUserMessage(ChatRequest request) {
		if (!hasAttachedDocuments(request) && !Boolean.TRUE.equals(request.webSearch())) {
			return new PromptedUserMessage(request.message(), List.of());
		}

		StringBuilder builder = new StringBuilder();
		builder.append("User question:\n");
		builder.append(request.message());

		appendAttachedDocuments(builder, request);
		List<WebSearchResult> sources = appendWebSearchResults(builder, request);

		return new PromptedUserMessage(builder.toString().trim(), sources);
	}

	private boolean hasAttachedDocuments(ChatRequest request) {
		return request.documentIds() != null && !request.documentIds().isEmpty();
	}

	private void appendAttachedDocuments(StringBuilder builder, ChatRequest request) {
		if (!hasAttachedDocuments(request)) {
			return;
		}

		List<ChatDocument> documents = chatDocumentRepository.findAllByIds(request.documentIds());
		if (documents.size() != request.documentIds().size()) {
			throw new IllegalArgumentException("One or more documents could not be found");
		}

		builder.append("\n\nAttached documents:\n\n");

		for (int index = 0; index < documents.size(); index++) {
			ChatDocument document = documents.get(index);
			builder.append("[Document ")
					.append(index + 1)
					.append(": ")
					.append(document.filename())
					.append("]\n")
					.append(document.contentText())
					.append("\n\n");
		}
	}

	private List<WebSearchResult> appendWebSearchResults(StringBuilder builder, ChatRequest request) {
		if (!Boolean.TRUE.equals(request.webSearch())) {
			return List.of();
		}

		List<WebSearchResult> results = searchWeb(request.message());
		if (results.isEmpty()) {
			return List.of();
		}

		builder.append("\n\nWeb search results:\n\n");
		for (int index = 0; index < results.size(); index++) {
			WebSearchResult result = results.get(index);
			builder.append("[")
					.append(index + 1)
					.append("] ")
					.append(result.title())
					.append("\nURL: ")
					.append(result.url())
					.append("\nContent: ")
					.append(result.content())
					.append("\n\n");
		}

		builder.append("Instructions:\n")
				.append("Use the web search results when relevant. Cite source URLs explicitly. ")
				.append("If the search results do not answer the question, say so.");

		return results;
	}

	private ServerSentEvent<List<WebSearchResult>> buildSourcesEvent(List<WebSearchResult> sources) {
		if (sources == null || sources.isEmpty()) {
			return null;
		}

		return ServerSentEvent.<List<WebSearchResult>>builder(sources)
				.event("sources")
				.build();
	}

	private List<WebSearchResult> searchWeb(String query) {
		try {
			return webSearchClient.search(query);
		}
		catch (RuntimeException exception) {
			return List.of();
		}
	}

	private Mono<Void> persistMessage(String conversationId, ConversationMessageRole role, String content) {
		return Mono.fromRunnable(() -> conversationHistoryRepository.addMessage(conversationId, role, content))
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}

	private Mono<Void> persistAssistantMessage(String conversationId, StringBuilder assistantResponse, List<WebSearchResult> sources) {
		if (assistantResponse.isEmpty()) {
			return Mono.empty();
		}

		String message = assistantResponse.toString();
		return Mono.fromRunnable(() -> conversationHistoryRepository.addMessage(conversationId, ConversationMessageRole.ASSISTANT, message, sources))
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}

	private String resolveModel(ChatRequest request) {
		if (request.model() != null && !request.model().isBlank()) {
			return request.model();
		}

		return defaultModel;
	}

	private String normalizeRequiredName(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("name is required");
		}

		return value.trim();
	}

	private record PromptedUserMessage(String content, List<WebSearchResult> sources) {
	}
}
