package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
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
	private static final String TABLE_NAME = "public.chat_document_vector_store";

	private final VectorStore vectorStore;
	private final JdbcTemplate jdbcTemplate;

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

		UUID id = parseDocumentId(documentId);
		if (id == null) {
			return Optional.empty();
		}

		List<ChatDocument> documents = jdbcTemplate.query("""
				SELECT
					id::text AS document_id,
					content,
					metadata->>'filename' AS filename,
					metadata->>'mediaType' AS media_type,
					metadata->>'createdAt' AS created_at
				FROM %s
				WHERE id = ?
				""".formatted(TABLE_NAME),
				(resultSet, rowNumber) -> new ChatDocument(
						resultSet.getString("document_id"),
						resultSet.getString("filename"),
						resultSet.getString("media_type"),
						resultSet.getString("content"),
						LocalDateTime.parse(resultSet.getString("created_at"))),
					id);

		return documents.stream().findFirst();
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

		UUID id = parseDocumentId(documentId);
		if (id == null) {
			return false;
		}

		return jdbcTemplate.update("DELETE FROM " + TABLE_NAME + " WHERE id = ?", id) > 0;
	}

	private UUID parseDocumentId(String documentId) {
		try {
			return UUID.fromString(documentId);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

}
