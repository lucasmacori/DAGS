package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "chat.documents", name = "provider", havingValue = "pgvector")
public class PgVectorChatDocumentRepository implements IChatDocumentRepository {

	private static final String QUERY_PLACEHOLDER = "document";
	private static final String METADATA_CREATED_AT = "createdAt";
	private static final String METADATA_DOCUMENT_ID = "documentId";
	private static final String METADATA_FILENAME = "filename";
	private static final String METADATA_MEDIA_TYPE = "mediaType";

	private final VectorStore vectorStore;

	@Override
	public ChatDocument save(ChatDocument document) {
		Document vectorDocument = Document.builder()
				.id(document.documentId())
				.text(document.contentText())
				.metadata(Map.of(
						METADATA_DOCUMENT_ID, document.documentId(),
						METADATA_FILENAME, document.filename(),
						METADATA_MEDIA_TYPE, document.mediaType(),
						METADATA_CREATED_AT, document.createdAt().toString()))
				.build();

		vectorStore.add(List.of(vectorDocument));
		return document;
	}

	@Override
	public Optional<ChatDocument> findById(String documentId) {
		if (documentId == null || documentId.isBlank()) {
			return Optional.empty();
		}

		FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
				.query(QUERY_PLACEHOLDER)
				.topK(1)
				.similarityThresholdAll()
				.filterExpression(filterExpressionBuilder.eq(METADATA_DOCUMENT_ID, documentId).build())
				.build());

		if (documents.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(toChatDocument(documents.getFirst()));
	}

	@Override
	public List<ChatDocument> findAllByIds(List<String> documentIds) {
		List<ChatDocument> foundDocuments = new ArrayList<>();
		if (documentIds == null) {
			return foundDocuments;
		}

		for (String documentId : documentIds) {
			findById(documentId).ifPresent(foundDocuments::add);
		}

		return foundDocuments;
	}

	@Override
	public boolean deleteById(String documentId) {
		if (documentId == null || documentId.isBlank()) {
			return false;
		}

		vectorStore.delete(documentId);
		return true;
	}

	private ChatDocument toChatDocument(Document document) {
		Map<String, Object> metadata = document.getMetadata();
		String createdAtRaw = String.valueOf(metadata.getOrDefault(METADATA_CREATED_AT, LocalDateTime.now().toString()));
		return new ChatDocument(
				String.valueOf(metadata.getOrDefault(METADATA_DOCUMENT_ID, document.getId())),
				String.valueOf(metadata.getOrDefault(METADATA_FILENAME, document.getId())),
				String.valueOf(metadata.getOrDefault(METADATA_MEDIA_TYPE, "application/octet-stream")),
				document.getText(),
				LocalDateTime.parse(createdAtRaw));
	}
}
