package fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;

@Table("source")
public class SourceEntity implements Persistable<UUID> {

	@Id
	private UUID sourceId;
	private String type;
	private String title;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime articleReadAt;
	private UUID userId;
	private String articleContent;
	private LocalDateTime summarizedAt;

	@Transient
	private boolean isNew;

	@PersistenceCreator
	public SourceEntity(UUID sourceId, String type, String title, String content, LocalDateTime createdAt,
			LocalDateTime updatedAt, LocalDateTime articleReadAt, UUID userId, String articleContent,
			LocalDateTime summarizedAt) {
		this(sourceId, type, title, content, createdAt, updatedAt, articleReadAt, userId, articleContent, summarizedAt, false);
	}

	private SourceEntity(UUID sourceId, String type, String title, String content, LocalDateTime createdAt,
			LocalDateTime updatedAt, LocalDateTime articleReadAt, UUID userId, String articleContent,
			LocalDateTime summarizedAt, boolean isNew) {
		this.sourceId = sourceId;
		this.type = type;
		this.title = title;
		this.content = content;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.articleReadAt = articleReadAt;
		this.userId = userId;
		this.articleContent = articleContent;
		this.summarizedAt = summarizedAt;
		this.isNew = isNew;
	}

	public static SourceEntity fromNewSource(Source source) {
		return new SourceEntity(
				UUID.fromString(source.sourceId()),
				source.type().name(),
				source.title(),
				source.content(),
				source.createdAt(),
				source.updatedAt(),
				source.articleReadAt(),
				source.userId() != null ? UUID.fromString(source.userId()) : null,
				source.articleContent(),
				source.summarizedAt(),
				true);
	}

	public static SourceEntity fromExistingSource(Source source) {
		return new SourceEntity(
				UUID.fromString(source.sourceId()),
				source.type().name(),
				source.title(),
				source.content(),
				source.createdAt(),
				source.updatedAt(),
				source.articleReadAt(),
				source.userId() != null ? UUID.fromString(source.userId()) : null,
				source.articleContent(),
				source.summarizedAt(),
				false);
	}

	public Source toSource() {
		return new Source(
				sourceId.toString(),
				SourceType.valueOf(type),
				title,
				content,
				createdAt,
				updatedAt,
				articleReadAt,
				userId != null ? userId.toString() : null,
				articleContent,
				summarizedAt);
	}

	@Override
	public UUID getId() {
		return sourceId;
	}

	@Override
	@Transient
	public boolean isNew() {
		return isNew;
	}
}
