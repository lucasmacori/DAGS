package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationHistoryPage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessage;
import fr.lucasmacori.ai_tools_api.chat.domain.model.ConversationMessageRole;
import fr.lucasmacori.ai_tools_api.chat.domain.model.WebSearchResult;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConversationHistoryRepository implements IConversationHistoryRepository {

	private static final TypeReference<List<WebSearchResult>> SOURCES_TYPE = new TypeReference<>() {
	};

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void addMessage(String conversationId, ConversationMessageRole role, String content, List<WebSearchResult> sources) {
		MapSqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("message_id", UUID.randomUUID())
				.addValue("conversation_id", UUID.fromString(conversationId))
				.addValue("role", role.name())
				.addValue("content", content)
				.addValue("sources_json", serializeSources(sources))
				.addValue("created_at", LocalDateTime.now());

		jdbcTemplate.update(
				"""
				INSERT INTO conversation_message (message_id, conversation_id, role, content, sources_json, created_at)
				VALUES (:message_id, :conversation_id, :role, :content, CAST(:sources_json AS jsonb), :created_at)
				""",
				parameters);
	}

	@Override
	public ConversationHistoryPage getConversationHistory(String conversationId, int page, int size) {
		int sanitizedPage = Math.max(page, 0);
		int sanitizedSize = Math.max(size, 1);
		int offset = sanitizedPage * sanitizedSize;

		Map<String, Object> parameters = Map.of(
				"conversation_id", UUID.fromString(conversationId),
				"limit", sanitizedSize,
				"offset", offset);

		List<ConversationMessage> messages = jdbcTemplate.query(
				"""
				SELECT message_id, conversation_id, role, content, sources_json, created_at
				FROM conversation_message
				WHERE conversation_id = :conversation_id
				ORDER BY created_at DESC, message_id DESC
				LIMIT :limit OFFSET :offset
				""",
				parameters,
				new ConversationMessageRowMapper(objectMapper));

		return new ConversationHistoryPage(sanitizedPage, sanitizedSize, messages);
	}

	@Override
	public void deleteConversationHistory(String conversationId) {
		Map<String, Object> parameters = Map.of("conversation_id", UUID.fromString(conversationId), "conversation_id_text", conversationId);

		jdbcTemplate.update(
				"DELETE FROM conversation_message WHERE conversation_id = :conversation_id",
				parameters);
		jdbcTemplate.update(
				"DELETE FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = :conversation_id_text",
				parameters);
	}

	private String serializeSources(List<WebSearchResult> sources) {
		try {
			return objectMapper.writeValueAsString(sources == null ? List.of() : sources);
		}
		catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize conversation message sources", exception);
		}
	}

	private static final class ConversationMessageRowMapper implements RowMapper<ConversationMessage> {

		private final ObjectMapper objectMapper;

		private ConversationMessageRowMapper(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public ConversationMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
			Timestamp createdAt = rs.getTimestamp("created_at");
			return new ConversationMessage(
					rs.getObject("message_id", UUID.class).toString(),
					rs.getObject("conversation_id", UUID.class).toString(),
					ConversationMessageRole.valueOf(rs.getString("role")),
					rs.getString("content"),
					readSources(rs.getString("sources_json")),
					createdAt == null ? null : createdAt.toLocalDateTime());
		}

		private List<WebSearchResult> readSources(String sourcesJson) {
			if (sourcesJson == null || sourcesJson.isBlank()) {
				return List.of();
			}

			try {
				return objectMapper.readValue(sourcesJson, SOURCES_TYPE);
			}
			catch (JsonProcessingException exception) {
				return List.of();
			}
		}
	}
}
