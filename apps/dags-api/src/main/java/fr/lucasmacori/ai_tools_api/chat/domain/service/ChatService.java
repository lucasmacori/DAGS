package fr.lucasmacori.ai_tools_api.chat.domain.service;

import java.util.List;
import java.util.Optional;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatRequest;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
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

	public Flux<String> chat(ChatRequest request) {
		return chat(request, systemPrompt);
	}

	public Flux<String> chat(ChatRequest request, String systemPrompt) {
		StringBuilder assistantResponse = new StringBuilder();
		String userMessage = buildUserMessage(request);
		Flux<String> generatedResponse = chatGenerator.stream(
				request.chatId(),
				systemPrompt,
				userMessage,
				resolveModel(request))
				.doOnNext(assistantResponse::append);

		return persistMessage(request.chatId(), ConversationMessageRole.USER, request.message())
				.thenMany(generatedResponse.concatWith(Mono.defer(() -> persistAssistantMessage(request.chatId(), assistantResponse).then(Mono.empty()))));
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

	private String buildUserMessage(ChatRequest request) {
		if (request.documentIds() == null || request.documentIds().isEmpty()) {
			return request.message();
		}

		List<ChatDocument> documents = chatDocumentRepository.findAllByIds(request.documentIds());
		if (documents.size() != request.documentIds().size()) {
			throw new IllegalArgumentException("One or more documents could not be found");
		}

		StringBuilder builder = new StringBuilder();
		builder.append("User question:\n");
		builder.append(request.message());
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

		return builder.toString().trim();
	}

	private Mono<Void> persistMessage(String conversationId, ConversationMessageRole role, String content) {
		return Mono.fromRunnable(() -> conversationHistoryRepository.addMessage(conversationId, role, content))
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}

	private Mono<Void> persistAssistantMessage(String conversationId, StringBuilder assistantResponse) {
		if (assistantResponse.isEmpty()) {
			return Mono.empty();
		}

		String message = assistantResponse.toString();
		return persistMessage(conversationId, ConversationMessageRole.ASSISTANT, message);
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
}
