package fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;

@Table("user_briefing_settings")
public class UserBriefingSettingsEntity implements Persistable<UUID> {

	@Id
	private UUID userId;
	private boolean enabled;
	private String frequency;
	private String generationTime;
	private String systemPrompt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Transient
	private boolean isNew;

	@PersistenceCreator
	public UserBriefingSettingsEntity(UUID userId, boolean enabled, String frequency, String generationTime,
			String systemPrompt, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this(userId, enabled, frequency, generationTime, systemPrompt, createdAt, updatedAt, false);
	}

	private UserBriefingSettingsEntity(UUID userId, boolean enabled, String frequency, String generationTime,
			String systemPrompt, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isNew) {
		this.userId = userId;
		this.enabled = enabled;
		this.frequency = frequency;
		this.generationTime = generationTime;
		this.systemPrompt = systemPrompt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.isNew = isNew;
	}

	public static UserBriefingSettingsEntity fromSettings(UserBriefingSettings settings, boolean isNew) {
		return new UserBriefingSettingsEntity(
				UUID.fromString(settings.userId()),
				settings.enabled(),
				settings.frequency(),
				settings.generationTime(),
				settings.systemPrompt(),
				settings.createdAt(),
				settings.updatedAt(),
				isNew);
	}

	public UserBriefingSettings toSettings() {
		return new UserBriefingSettings(
				userId.toString(),
				enabled,
				frequency,
				generationTime,
				systemPrompt,
				createdAt,
				updatedAt);
	}

	@Override
	public UUID getId() {
		return userId;
	}

	@Override
	@Transient
	public boolean isNew() {
		return isNew;
	}
}
