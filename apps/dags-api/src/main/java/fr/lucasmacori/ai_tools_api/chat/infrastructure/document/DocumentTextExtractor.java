package fr.lucasmacori.ai_tools_api.chat.infrastructure.document;

public interface DocumentTextExtractor {
	String extractText(String filename, String mediaType, byte[] content);
}
