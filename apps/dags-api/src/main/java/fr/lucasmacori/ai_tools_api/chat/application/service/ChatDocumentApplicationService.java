package fr.lucasmacori.ai_tools_api.chat.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.document.DocumentTextExtractor;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.config.ChatPromptProperties;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.core.io.buffer.DataBufferUtils;

@Service
@RequiredArgsConstructor
public class ChatDocumentApplicationService {

	private final IChatDocumentRepository chatDocumentRepository;
	private final DocumentTextExtractor documentTextExtractor;
	private final ChatPromptProperties chatPromptProperties;

	public Mono<List<ChatDocument>> uploadDocuments(Flux<FilePart> files) {
		return files.flatMap(this::storeDocument).collectList();
	}

	public Mono<Void> deleteDocument(String documentId) {
		return Mono.fromRunnable(() -> {
			if (!chatDocumentRepository.deleteById(documentId)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
			}
		})
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}

	private Mono<ChatDocument> storeDocument(FilePart filePart) {
		String filename = filePart.filename();

		if (filename == null || filename.isBlank()) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "filename is required"));
		}

		String lowerCaseName = filename.toLowerCase();
		if (!(lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".md") || lowerCaseName.endsWith(".pdf"))) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported document type"));
		}

		return DataBufferUtils.join(filePart.content())
				.flatMap(dataBuffer -> {
					byte[] content;
					try {
						content = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(content);
					} finally {
						DataBufferUtils.release(dataBuffer);
					}

					String mediaType = filePart.headers().getContentType() != null
							? filePart.headers().getContentType().toString()
							: "application/octet-stream";

					return Mono.fromCallable(() -> createAndSaveDocument(filename, mediaType, content))
							.subscribeOn(Schedulers.boundedElastic());
				});
	}

	private ChatDocument createAndSaveDocument(String filename, String mediaType, byte[] content) {
		if (content.length == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
		}
		if (content.length > chatPromptProperties.maxDocumentFileSizeBytes()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 10MB limit");
		}

		String extractedText;
		try {
			extractedText = documentTextExtractor.extractText(filename, mediaType, content);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}

		if (extractedText.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document contains no readable text");
		}

		if (extractedText.length() > chatPromptProperties.maxDocumentCharacters()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extracted text exceeds configured character limit");
		}

		ChatDocument document = new ChatDocument(
				UUID.randomUUID().toString(),
				filename,
				mediaType,
				extractedText,
				LocalDateTime.now());
		return chatDocumentRepository.save(document);
	}
}
