package fr.lucasmacori.ai_tools_api.chat.infrastructure.document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class TikaDocumentTextExtractor implements DocumentTextExtractor {

	private static final Tika TIKA = new Tika();

	@Override
	public String extractText(String filename, String mediaType, byte[] content) {
		String lowerCaseName = filename == null ? "" : filename.toLowerCase(Locale.ROOT);

		try {
			if (lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".md")) {
				return new String(content, StandardCharsets.UTF_8).trim();
			}

			if (lowerCaseName.endsWith(".pdf")) {
				return TIKA.parseToString(new ByteArrayInputStream(content)).trim();
			}
		} catch (Exception exception) {
			throw new IllegalArgumentException("Could not extract text from document", exception);
		}

		throw new IllegalArgumentException("Unsupported document type");
	}
}
