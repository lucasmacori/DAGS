package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ChatDocument;

class PgVectorChatDocumentRepositoryTest {

	private final VectorStore vectorStore = mock(VectorStore.class);
	private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
	private final PgVectorChatDocumentRepository repository = new PgVectorChatDocumentRepository(vectorStore, jdbcTemplate);

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void findByIdUsesExactJdbcLookupWithoutVectorSearch() throws Exception {
		UUID documentId = UUID.randomUUID();
		LocalDateTime createdAt = LocalDateTime.parse("2026-05-01T10:15:30");

		when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
				.thenAnswer(invocation -> {
					RowMapper<ChatDocument> rowMapper = invocation.getArgument(1);
					ResultSet resultSet = mock(ResultSet.class);
					when(resultSet.getString("document_id")).thenReturn(documentId.toString());
					when(resultSet.getString("filename")).thenReturn("notes.txt");
					when(resultSet.getString("media_type")).thenReturn("text/plain");
					when(resultSet.getString("content")).thenReturn("hello");
					when(resultSet.getString("created_at")).thenReturn(createdAt.toString());
					return List.of(rowMapper.mapRow(resultSet, 0));
				});

		Optional<ChatDocument> document = repository.findById(documentId.toString());

		assertThat(document).contains(new ChatDocument(
				documentId.toString(),
				"notes.txt",
				"text/plain",
				"hello",
				createdAt));
		verify(jdbcTemplate).query(anyString(), any(RowMapper.class), any());
		verifyNoInteractions(vectorStore);
	}

	@Test
	void findByIdReturnsEmptyForInvalidUuidWithoutQuerying() {
		assertThat(repository.findById("doc-1")).isEmpty();

		verifyNoInteractions(jdbcTemplate, vectorStore);
	}

	@Test
	void deleteByIdUsesExactJdbcDeleteWithoutVectorStoreFilterParsing() {
		UUID documentId = UUID.randomUUID();
		when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

		assertThat(repository.deleteById(documentId.toString())).isTrue();

		verify(jdbcTemplate).update(anyString(), any(Object[].class));
		verifyNoInteractions(vectorStore);
	}

	@Test
	void deleteByIdReturnsFalseForInvalidUuidWithoutQuerying() {
		assertThat(repository.deleteById("doc-1")).isFalse();

		verifyNoInteractions(jdbcTemplate, vectorStore);
	}
}
