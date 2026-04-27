package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;

@Repository
public class InMemoryChatDocumentRepository implements IChatDocumentRepository {

	private final ConcurrentHashMap<String, ChatDocument> documents = new ConcurrentHashMap<>();

	@Override
	public ChatDocument save(ChatDocument document) {
		documents.put(document.documentId(), document);
		return document;
	}

	@Override
	public Optional<ChatDocument> findById(String documentId) {
		return Optional.ofNullable(documents.get(documentId));
	}

	@Override
	public List<ChatDocument> findAllByIds(List<String> documentIds) {
		List<ChatDocument> foundDocuments = new ArrayList<>();
		if (documentIds == null) {
			return foundDocuments;
		}
		for (String documentId : documentIds) {
			if (documentId == null || documentId.isBlank()) {
				continue;
			}
			ChatDocument document = documents.get(documentId);
			if (document != null) {
				foundDocuments.add(document);
			}
		}
		return foundDocuments;
	}

	@Override
	public boolean deleteById(String documentId) {
		return documents.remove(documentId) != null;
	}
}
