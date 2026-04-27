package fr.lucasmacori.ai_tools_api.chat.domain.repository;

import java.util.List;
import java.util.Optional;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;

public interface IChatDocumentRepository {
	ChatDocument save(ChatDocument document);
	Optional<ChatDocument> findById(String documentId);
	List<ChatDocument> findAllByIds(List<String> documentIds);
	boolean deleteById(String documentId);
}
