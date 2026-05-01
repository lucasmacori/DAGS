package fr.lucasmacori.ai_tools_api.chat.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IChatDocumentRepository;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.config.ChatPromptProperties;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.config.ChatPromptProperties.Documents;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.config.ChatPromptProperties.Prompt;
import fr.lucasmacori.ai_tools_api.chat.infrastructure.document.DocumentTextExtractor;
import reactor.core.publisher.Flux;

class ChatDocumentApplicationServiceTest {

	@Test
	void uploadDocumentsRunsBlockingDocumentStorageOnBoundedElastic() {
		AtomicReference<String> extractorThreadName = new AtomicReference<>();
		AtomicReference<String> repositoryThreadName = new AtomicReference<>();

		DocumentTextExtractor documentTextExtractor = (filename, mediaType, content) -> {
			extractorThreadName.set(Thread.currentThread().getName());
			return "hello world";
		};
		IChatDocumentRepository chatDocumentRepository = new RecordingChatDocumentRepository(repositoryThreadName);
		ChatDocumentApplicationService service = new ChatDocumentApplicationService(
				chatDocumentRepository,
				documentTextExtractor,
				new ChatPromptProperties(
						"model",
						new Prompt("system"),
						null,
						new Documents("in-memory", 200_000, 10L * 1024L * 1024L)));

		List<ChatDocument> documents = service.uploadDocuments(Flux.just(filePart("notes.txt", "hello"))).block();

		assertThat(documents).hasSize(1);
		assertThat(extractorThreadName.get()).contains("boundedElastic");
		assertThat(repositoryThreadName.get()).contains("boundedElastic");
	}

	private FilePart filePart(String filename, String content) {
		FilePart filePart = mock(FilePart.class);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		when(filePart.filename()).thenReturn(filename);
		when(filePart.headers()).thenReturn(headers);
		when(filePart.content()).thenReturn(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(content.getBytes())));
		return filePart;
	}

	private static class RecordingChatDocumentRepository implements IChatDocumentRepository {

		private final AtomicReference<String> repositoryThreadName;

		private RecordingChatDocumentRepository(AtomicReference<String> repositoryThreadName) {
			this.repositoryThreadName = repositoryThreadName;
		}

		@Override
		public ChatDocument save(ChatDocument document) {
			repositoryThreadName.set(Thread.currentThread().getName());
			return document;
		}

		@Override
		public Optional<ChatDocument> findById(String documentId) {
			return Optional.empty();
		}

		@Override
		public List<ChatDocument> findAllByIds(List<String> documentIds) {
			return List.of();
		}

		@Override
		public boolean deleteById(String documentId) {
			return false;
		}
	}
}
