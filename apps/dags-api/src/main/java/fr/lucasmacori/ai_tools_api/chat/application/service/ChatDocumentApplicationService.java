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
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBufferUtils;

@Service
@RequiredArgsConstructor
public class ChatDocumentApplicationService {

	private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;

	private final IChatDocumentRepository chatDocumentRepository;
	private final DocumentTextExtractor documentTextExtractor;

	public Mono<List<ChatDocument>> uploadDocuments(Flux<FilePart> files) {
		return files.flatMap(this::storeDocument).collectList();
	}

	public void deleteDocument(String documentId) {
		if (!chatDocumentRepository.deleteById(documentId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
		}
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
					try {
						byte[] content = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(content);
						if (content.length == 0) {
							return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty"));
						}
						if (content.length > MAX_FILE_SIZE_BYTES) {
							return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 10MB limit"));
						}

						String mediaType = filePart.headers().getContentType() != null
								? filePart.headers().getContentType().toString()
								: "application/octet-stream";
						String extractedText;
						try {
							extractedText = documentTextExtractor.extractText(filename, mediaType, content);
						} catch (IllegalArgumentException exception) {
							return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception));
						}

						if (extractedText.isBlank()) {
							return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document contains no readable text"));
						}

						ChatDocument document = new ChatDocument(
								UUID.randomUUID().toString(),
								filename,
								mediaType,
								extractedText,
								LocalDateTime.now());
						return Mono.just(chatDocumentRepository.save(document));
					} finally {
						DataBufferUtils.release(dataBuffer);
					}
				});
	}
}
