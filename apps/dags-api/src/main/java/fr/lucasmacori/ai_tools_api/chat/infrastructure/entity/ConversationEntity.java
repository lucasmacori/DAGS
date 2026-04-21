package fr.lucasmacori.ai_tools_api.chat.infrastructure.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEntity implements Persistable<UUID> {
	@Id
	private UUID conversationId;
	private String conversationName;
	private LocalDateTime createdAt;

	@Transient
	@Builder.Default
	private boolean isNew = false;

	@Override
	public UUID getId() {
		return conversationId;
	}

	@Override
	@Transient
	public boolean isNew() {
		return this.isNew;
	}

	public Conversation toConversation() {
		return new Conversation(this.conversationId.toString(), this.conversationName, this.createdAt);
	}

	public static ConversationEntity fromName(final String name) {
		return ConversationEntity.builder()
				.conversationId(UUID.randomUUID())
				.conversationName(name)
				.createdAt(LocalDateTime.now())
				.isNew(true)
				.build();
	}

	public static ConversationEntity fromExistingConversation(final Conversation conversation) {
		return ConversationEntity.builder()
				.conversationId(UUID.fromString(conversation.conversationId()))
				.conversationName(conversation.conversationName())
				.createdAt(conversation.createdAt())
				.isNew(false)
				.build();
	}
}
