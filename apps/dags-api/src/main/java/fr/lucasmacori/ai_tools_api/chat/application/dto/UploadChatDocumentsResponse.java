package fr.lucasmacori.ai_tools_api.chat.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;

public record UploadChatDocumentsResponse(
		@JsonProperty("documents") List<ChatDocumentItemResponse> documents) {

	public static UploadChatDocumentsResponse from(List<ChatDocument> documents) {
		return new UploadChatDocumentsResponse(documents.stream().map(ChatDocumentItemResponse::from).toList());
	}
}
