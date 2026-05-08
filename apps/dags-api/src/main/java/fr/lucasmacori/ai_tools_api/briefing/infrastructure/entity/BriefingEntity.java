package fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Briefing;

@Table("briefing")
public class BriefingEntity implements Persistable<UUID> {

	@Id
	private UUID briefingId;
	private UUID userId;
	private String content;
	private int articleCount;
	private LocalDateTime createdAt;

	@Transient
	private boolean isNew;

	@PersistenceCreator
	public BriefingEntity(UUID briefingId, UUID userId, String content, int articleCount, LocalDateTime createdAt) {
		this(briefingId, userId, content, articleCount, createdAt, false);
	}

	private BriefingEntity(UUID briefingId, UUID userId, String content, int articleCount, LocalDateTime createdAt, boolean isNew) {
		this.briefingId = briefingId;
		this.userId = userId;
		this.content = content;
		this.articleCount = articleCount;
		this.createdAt = createdAt;
		this.isNew = isNew;
	}

	public static BriefingEntity fromBriefing(Briefing briefing) {
		return new BriefingEntity(
				UUID.fromString(briefing.briefingId()),
				UUID.fromString(briefing.userId()),
				briefing.content(),
				briefing.articleCount(),
				briefing.createdAt(),
				true);
	}

	public Briefing toBriefing() {
		return new Briefing(
				briefingId.toString(),
				userId.toString(),
				content,
				articleCount,
				createdAt);
	}

	@Override
	public UUID getId() {
		return briefingId;
	}

	@Override
	@Transient
	public boolean isNew() {
		return isNew;
	}
}
