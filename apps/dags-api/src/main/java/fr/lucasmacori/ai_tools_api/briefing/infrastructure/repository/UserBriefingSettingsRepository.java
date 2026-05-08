package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IUserBriefingSettingsRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.UserBriefingSettingsEntity;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc.UserBriefingSettingsJDBCRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserBriefingSettingsRepository implements IUserBriefingSettingsRepository {

	private final UserBriefingSettingsJDBCRepository jdbcRepository;

	@Override
	public Optional<UserBriefingSettings> findByUserId(String userId) {
		return jdbcRepository.findByUserId(UUID.fromString(userId))
				.map(UserBriefingSettingsEntity::toSettings);
	}

	@Override
	public UserBriefingSettings save(UserBriefingSettings settings) {
		boolean isNew = jdbcRepository.findByUserId(UUID.fromString(settings.userId())).isEmpty();
		return jdbcRepository.save(UserBriefingSettingsEntity.fromSettings(settings, isNew)).toSettings();
	}
}
