package fr.lucasmacori.ai_tools_api.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;

public record ChatDocumentItemResponse(
		@JsonProperty("document_id") String documentId,
		@JsonProperty("filename") String filename,
		@JsonProperty("media_type") String mediaType,
		@JsonProperty("character_count") int characterCount,
		@JsonProperty("preview") String preview) {

	public static ChatDocumentItemResponse from(ChatDocument document) {
		String preview = document.contentText().length() > 200
				? document.contentText().substring(0, 200)
				: document.contentText();

		return new ChatDocumentItemResponse(
				document.documentId(),
				document.filename(),
				document.mediaType(),
				document.contentText().length(),
				preview);
	}
}
