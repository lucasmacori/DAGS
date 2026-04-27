package fr.lucasmacori.ai_tools_api.chat.domain.model;

import java.time.LocalDateTime;

public record ChatDocument(
		String documentId,
		String filename,
		String mediaType,
		String contentText,
		LocalDateTime createdAt) {
}
